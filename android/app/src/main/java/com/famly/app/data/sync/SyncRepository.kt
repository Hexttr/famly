package com.famly.app.data.sync

import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
import com.famly.app.data.local.entity.PendingSyncEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.remote.FamlyApiClient
import com.famly.app.data.remote.HouseholdMemberDto
import com.famly.app.data.remote.InviteResult
import com.famly.app.data.remote.SyncEntityDto
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.util.UUID

data class SyncStatus(
    val success: Boolean,
    val pushedCount: Int = 0,
    val pulledCount: Int = 0,
    val error: String? = null,
)

/**
 * Offline-first sync: persists pending changes in Room, pushes delta when online.
 */
class SyncRepository(
    private val api: FamlyApiClient,
    private val db: FamlyDatabase,
    private val preferences: UserPreferences,
) {
    private var cachedInvite: InviteResult? = null
    private var onScheduleSync: (() -> Unit)? = null

    fun setOnScheduleSync(callback: () -> Unit) {
        onScheduleSync = callback
    }

    private fun scheduleSync() {
        onScheduleSync?.invoke()
    }

    suspend fun register(email: String, password: String, displayName: String): SyncStatus =
        runCatching {
            val result = api.register(email, password, displayName)
            preferences.setAuthSession(result.token, result.userId)
            restoreHouseholdFromServer(result.token)
            refreshPremiumStatus(result.token)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun login(email: String, password: String): SyncStatus =
        runCatching {
            val result = api.login(email, password)
            preferences.setAuthSession(result.token, result.userId)
            restoreHouseholdFromServer(result.token)
            refreshPremiumStatus(result.token)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun logout(): SyncStatus = runCatching {
        val token = preferences.settings.first().authToken
        if (!token.isNullOrBlank()) {
            runCatching { api.logout(token) }
        }
        preferences.clearAuthSession()
        db.familyMemberDao().deleteAll()
        db.pendingSyncDao().deleteAll()
        cachedInvite = null
        SyncStatus(success = true)
    }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun leaveHousehold(): SyncStatus = runCatching {
        val token = requireAuthToken()
        api.leaveHousehold(token)
        preferences.clearHouseholdSession()
        db.familyMemberDao().deleteAll()
        cachedInvite = null
        SyncStatus(success = true)
    }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun createHousehold(name: String): SyncStatus =
        runCatching {
            val token = requireAuthToken()
            val household = api.createHousehold(token, name)
            preferences.setHouseholdId(household.id)
            preferences.setHouseholdName(household.name)
            syncHouseholdMembers(token, household.id)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun joinHousehold(inviteCode: String): SyncStatus =
        runCatching {
            val token = requireAuthToken()
            val household = api.joinHousehold(token, inviteCode)
            preferences.setHouseholdId(household.id)
            preferences.setHouseholdName(household.name)
            syncHouseholdMembers(token, household.id)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun enqueue(entity: SyncEntityDto) {
        if (entity.type == "family_member") return
        val key = "${entity.type}:${entity.id}"
        db.pendingSyncDao().upsert(
            PendingSyncEntity(
                compositeKey = key,
                type = entity.type,
                entityId = entity.id,
                payload = entity.payload,
                syncVersion = entity.syncVersion,
                updatedAt = entity.updatedAt,
                deleted = entity.deleted,
            ),
        )
        scheduleSync()
    }

    suspend fun enqueueTransaction(tx: TransactionEntity) {
        db.categoryDao().getById(tx.categoryId)?.let { enqueueCategory(it, schedule = false) }
        db.accountDao().getById(tx.accountId)?.let { enqueueAccount(it, schedule = false) }
        enqueueEntity(tx.toSyncEntity(System.currentTimeMillis()))
        scheduleSync()
    }

    suspend fun enqueueAccount(account: AccountEntity, schedule: Boolean = true) {
        enqueueEntity(account.toSyncEntity(System.currentTimeMillis()))
        if (schedule) scheduleSync()
    }

    suspend fun enqueueCategory(category: CategoryEntity, schedule: Boolean = true) {
        enqueueEntity(category.toSyncEntity(System.currentTimeMillis()))
        if (schedule) scheduleSync()
    }

    private suspend fun enqueueEntity(entity: SyncEntityDto) {
        if (entity.type == "family_member") return
        val key = "${entity.type}:${entity.id}"
        db.pendingSyncDao().upsert(
            PendingSyncEntity(
                compositeKey = key,
                type = entity.type,
                entityId = entity.id,
                payload = entity.payload,
                syncVersion = entity.syncVersion,
                updatedAt = entity.updatedAt,
                deleted = entity.deleted,
            ),
        )
    }

    suspend fun enqueueFamilyMember(member: FamilyMemberEntity) {
        // Members are synced from server; local avatar changes stay local.
    }

    suspend fun enqueueIouBalance(balance: IouBalanceEntity) =
        enqueue(balance.toSyncEntity(System.currentTimeMillis()))

    suspend fun enqueueDeleted(type: String, id: String) {
        enqueueEntity(
            SyncEntityDto(
                type = type,
                id = id,
                payload = "{}",
                syncVersion = 1,
                updatedAt = System.currentTimeMillis(),
                deleted = true,
            ),
        )
        scheduleSync()
    }

    suspend fun updateFamilyMemberOnServer(member: FamilyMemberEntity) {
        val settings = preferences.settings.first()
        val token = settings.authToken ?: return
        val householdId = settings.householdId ?: return
        api.updateMember(
            token = token,
            householdId = householdId,
            memberId = member.id,
            role = member.role,
            visibility = member.visibility,
            displayName = member.name,
        )
        syncHouseholdMembers(token, householdId)
    }

    suspend fun updateProfileName(displayName: String) {
        val token = requireAuthToken()
        api.updateProfile(token, displayName.trim())
        val settings = preferences.settings.first()
        if (!settings.householdId.isNullOrBlank()) {
            syncHouseholdMembers(token, settings.householdId)
        }
    }

    suspend fun generateInviteCode(): InviteResult {
        ensureHouseholdLinked()
        val settings = preferences.settings.first()
        val token = settings.authToken ?: error("Войдите в аккаунт в Настройках")
        val householdId = settings.householdId ?: error("Не удалось создать семью")
        return api.generateInvite(token, householdId).also { cachedInvite = it }
    }

    fun cachedInviteUrl(): String? = cachedInvite?.inviteUrl

    suspend fun ensureHouseholdLinked() {
        val settings = preferences.settings.first()
        val token = settings.authToken ?: error("Войдите в аккаунт в Настройках")
        if (!settings.householdId.isNullOrBlank()) return
        restoreHouseholdFromServer(token)
        val updated = preferences.settings.first()
        if (!updated.householdId.isNullOrBlank()) return
        val household = api.createHousehold(token, updated.householdName?.trim().orEmpty().ifBlank { "Наша семья" })
        preferences.setHouseholdId(household.id)
        preferences.setHouseholdName(household.name)
        syncHouseholdMembers(token, household.id)
    }

    private suspend fun restoreHouseholdFromServer(token: String) {
        val settings = preferences.settings.first()
        if (!settings.householdId.isNullOrBlank()) {
            syncHouseholdMembers(token, settings.householdId!!)
            return
        }
        val household = api.getHousehold(token) ?: return
        preferences.setHouseholdId(household.id)
        preferences.setHouseholdName(household.name)
        syncHouseholdMembers(token, household.id)
    }

    suspend fun refreshPremiumStatus(token: String? = null) {
        if (!com.famly.app.domain.FamlyAccess.monetizationEnabled()) {
            preferences.setPremiumFromServer(isPremium = true, expiresAt = null)
            return
        }
        val authToken = token ?: preferences.settings.first().authToken
        if (authToken.isNullOrBlank()) return
        val status = api.getSubscriptionStatus(authToken)
        preferences.setPremiumFromServer(status.isPremium, status.expiresAt)
    }

    suspend fun sync(): SyncStatus {
        val token = preferences.settings.first().authToken
            ?: return SyncStatus(success = false, error = "Not authenticated")

        preferences.setLastSyncAttemptAt(System.currentTimeMillis())

        return runCatching {
            var settings = preferences.settings.first()
            if (settings.householdId.isNullOrBlank()) {
                val pendingName = settings.householdName?.trim().orEmpty()
                if (pendingName.isNotEmpty()) {
                    createHousehold(pendingName)
                    settings = preferences.settings.first()
                }
            }
            if (settings.householdId.isNullOrBlank()) {
                return SyncStatus(
                    success = false,
                    error = "Сначала создайте семью на странице «Семья»",
                )
            }

            refreshPremiumStatus(token)
            val needsSnapshot = !preferences.isSyncSnapshotQueued(settings.householdId!!)
            if (needsSnapshot) {
                queueHouseholdSnapshot(settings.householdId!!)
            }

            val since = settings.lastSyncToken ?: 0L
            val pending = loadPendingEntities()
            if (pending.isNotEmpty()) {
                api.push(token, pending)
            }

            val pull = api.pull(token, since)
            applyRemoteEntities(pull.entities)
            syncHouseholdMembers(token, settings.householdId!!)
            preferences.setLastSyncToken(pull.syncToken)
            db.pendingSyncDao().deleteAll()
            if (needsSnapshot) {
                preferences.setSyncSnapshotQueued(settings.householdId!!)
            }

            SyncStatus(
                success = true,
                pushedCount = pending.size,
                pulledCount = pull.entities.size,
            )
        }.getOrElse { SyncStatus(success = false, error = it.message) }
    }

    private suspend fun queueHouseholdSnapshot(householdId: String) {
        val now = System.currentTimeMillis()
        db.categoryDao().observeAll().first().forEach { enqueueEntity(it.toSyncEntity(now)) }
        db.accountDao().observeAll().first().forEach { enqueueEntity(it.toSyncEntity(now)) }
        db.transactionDao().observeAll().first().forEach { tx ->
            db.categoryDao().getById(tx.categoryId)?.let { enqueueEntity(it.toSyncEntity(now)) }
            db.accountDao().getById(tx.accountId)?.let { enqueueEntity(it.toSyncEntity(now)) }
            enqueueEntity(tx.toSyncEntity(now))
        }
    }

    private suspend fun loadPendingEntities(): List<SyncEntityDto> =
        db.pendingSyncDao().getAll().map { row ->
            SyncEntityDto(
                type = row.type,
                id = row.entityId,
                payload = row.payload,
                syncVersion = row.syncVersion,
                updatedAt = row.updatedAt,
                deleted = row.deleted,
            )
        }

    private suspend fun requireAuthToken(): String {
        val token = preferences.settings.first().authToken
        require(!token.isNullOrBlank()) { "Not authenticated" }
        return token
    }

    private suspend fun syncHouseholdMembers(token: String, householdId: String?) {
        if (householdId.isNullOrBlank()) return
        val household = api.getHousehold(token) ?: return
        val now = System.currentTimeMillis()
        val serverIds = household.members.map { it.id }.toSet()
        val localMembers = db.familyMemberDao().observeAll().first()
        localMembers.filter { it.id !in serverIds && !it.id.startsWith("local") }.forEach {
            db.familyMemberDao().delete(it.id)
        }
        household.members.forEach { dto ->
            val existing = localMembers.find { it.id == dto.id }
            val member = dto.toFamilyMemberEntity(householdId, now, existing?.avatar)
            db.familyMemberDao().upsert(member)
        }
    }

    private fun HouseholdMemberDto.toFamilyMemberEntity(
        householdId: String,
        now: Long,
        existingAvatar: String?,
    ) = FamilyMemberEntity(
        id = id,
        householdId = householdId,
        userId = userId,
        name = displayName,
        role = role,
        visibility = visibility,
        avatar = existingAvatar ?: memberAvatar(displayName),
        syncVersion = 0,
        createdAt = now,
        updatedAt = now,
    )

    private fun memberAvatar(name: String): String {
        val emojis = listOf("👨", "👩", "👦", "👧", "👴", "👵", "🧑")
        return emojis[name.hashCode().mod(emojis.size).let { if (it < 0) it + emojis.size else it }]
    }

    private suspend fun applyRemoteEntities(entities: List<SyncEntityDto>) {
        entities.forEach { entity ->
            if (entity.deleted) {
                when (entity.type) {
                    "account" -> db.accountDao().delete(entity.id)
                    "category" -> db.categoryDao().delete(entity.id)
                    "transaction" -> db.transactionDao().delete(entity.id)
                    "family_member" -> db.familyMemberDao().delete(entity.id)
                    "iou_balance" -> db.iouBalanceDao().delete(entity.id)
                }
                return@forEach
            }
            val payload = JSONObject(entity.payload)
            when (entity.type) {
                "account" -> db.accountDao().upsert(payload.toAccount())
                "category" -> db.categoryDao().upsert(payload.toCategory())
                "transaction" -> db.transactionDao().upsert(payload.toTransaction())
                "family_member" -> db.familyMemberDao().upsert(payload.toFamilyMember())
                "iou_balance" -> db.iouBalanceDao().upsert(payload.toIouBalance())
            }
        }
    }

    private fun AccountEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "account",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("icon", icon)
            put("balanceKopecks", balanceKopecks)
            put("color", color)
            put("sortOrder", sortOrder)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
    )

    private fun CategoryEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "category",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("icon", icon)
            put("type", type)
            put("color", color)
            put("budgetLimitKopecks", budgetLimitKopecks)
            put("rolloverKopecks", rolloverKopecks)
            put("rolloverEnabled", rolloverEnabled)
            put("sortOrder", sortOrder)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
    )

    private fun TransactionEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "transaction",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("amountKopecks", amountKopecks)
            put("type", type)
            put("categoryId", categoryId)
            put("accountId", accountId)
            put("dateEpochDay", dateEpochDay)
            put("note", note)
            put("isRecurring", isRecurring)
            put("recurringDay", recurringDay)
            put("lastRecurrenceEpochDay", lastRecurrenceEpochDay)
            put("isPrivate", isPrivate)
            put("splitMemberIds", splitMemberIds)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
    )

    private fun IouBalanceEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "iou_balance",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("fromMemberId", fromMemberId)
            put("toMemberId", toMemberId)
            put("amountKopecks", amountKopecks)
            put("settledAt", settledAt)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
        deleted = settledAt != null,
    )

    private fun JSONObject.toAccount() = AccountEntity(
        id = getString("id"),
        name = getString("name"),
        icon = getString("icon"),
        balanceKopecks = getLong("balanceKopecks"),
        color = getString("color"),
        sortOrder = optInt("sortOrder", 0),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )

    private fun JSONObject.toCategory() = CategoryEntity(
        id = getString("id"),
        name = getString("name"),
        icon = getString("icon"),
        type = getString("type"),
        color = getString("color"),
        budgetLimitKopecks = if (has("budgetLimitKopecks") && !isNull("budgetLimitKopecks")) {
            getLong("budgetLimitKopecks")
        } else null,
        rolloverKopecks = optLong("rolloverKopecks", 0),
        rolloverEnabled = optBoolean("rolloverEnabled", false),
        sortOrder = optInt("sortOrder", 0),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )

    private fun JSONObject.toTransaction() = TransactionEntity(
        id = getString("id"),
        amountKopecks = getLong("amountKopecks"),
        type = getString("type"),
        categoryId = getString("categoryId"),
        accountId = getString("accountId"),
        dateEpochDay = getLong("dateEpochDay"),
        note = if (has("note") && !isNull("note")) getString("note") else null,
        isRecurring = optBoolean("isRecurring", false),
        recurringDay = if (has("recurringDay") && !isNull("recurringDay")) getInt("recurringDay") else null,
        lastRecurrenceEpochDay = if (has("lastRecurrenceEpochDay") && !isNull("lastRecurrenceEpochDay")) {
            getLong("lastRecurrenceEpochDay")
        } else null,
        isPrivate = optBoolean("isPrivate", false),
        splitMemberIds = if (has("splitMemberIds") && !isNull("splitMemberIds")) {
            getString("splitMemberIds")
        } else null,
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )

    private fun JSONObject.toFamilyMember() = FamilyMemberEntity(
        id = getString("id"),
        householdId = getString("householdId"),
        userId = if (has("userId") && !isNull("userId")) getString("userId") else null,
        name = getString("name"),
        role = getString("role"),
        visibility = getString("visibility"),
        avatar = getString("avatar"),
        syncVersion = optLong("syncVersion", 0),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )

    private fun JSONObject.toIouBalance() = IouBalanceEntity(
        id = getString("id"),
        fromMemberId = getString("fromMemberId"),
        toMemberId = getString("toMemberId"),
        amountKopecks = getLong("amountKopecks"),
        settledAt = if (has("settledAt") && !isNull("settledAt")) getLong("settledAt") else null,
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )
}
