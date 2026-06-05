package com.famly.app.data.sync

import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.PendingSyncEntity
import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.remote.FamlyApiClient
import com.famly.app.data.remote.HouseholdMemberDto
import com.famly.app.data.remote.HouseholdSnapshotDto
import com.famly.app.data.remote.InviteResult
import com.famly.app.data.remote.SyncEntityDto
import com.famly.app.domain.DEFAULT_MEMBER_AVATAR
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
            preferences.setPendingHouseholdSnapshot(true)
            syncHouseholdMembers(token, household.id)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun joinHousehold(inviteCode: String): SyncStatus =
        runCatching {
            val token = requireAuthToken()
            val household = api.joinHousehold(token, inviteCode)
            preferences.setHouseholdId(household.id)
            preferences.setHouseholdName(household.name)
            preferences.setPendingHouseholdSnapshot(false)
            wipeLocalBudgetForJoin()
            preferences.clearLastSyncToken()
            sync()
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

    suspend fun updateMemberFields(
        memberId: String,
        role: String? = null,
        visibility: String? = null,
        displayName: String? = null,
        avatar: String? = null,
    ) {
        val settings = preferences.settings.first()
        val token = settings.authToken ?: error("Not authenticated")
        val householdId = settings.householdId ?: error("No household")
        api.updateMember(
            token = token,
            householdId = householdId,
            memberId = memberId,
            role = role,
            visibility = visibility,
            displayName = displayName,
            avatar = avatar,
        )
        syncHouseholdMembers(token, householdId)
    }

    suspend fun updateMemberAvatar(memberId: String, avatar: String) {
        updateMemberFields(memberId, avatar = avatar)
    }

    suspend fun updateFamilyMemberOnServer(member: FamilyMemberEntity) {
        updateMemberFields(
            memberId = member.id,
            role = member.role,
            visibility = member.visibility,
            displayName = member.name,
            avatar = member.avatar,
        )
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
        preferences.setPendingHouseholdSnapshot(true)
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

    suspend fun syncIfStale(maxAgeMs: Long = 30_000L): SyncStatus? {
        val lastAttempt = preferences.getLastSyncAttemptAt()
        if (System.currentTimeMillis() - lastAttempt < maxAgeMs) return null
        return sync()
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
            if (preferences.needsSyncCursorFix()) {
                preferences.applySyncCursorFix()
                settings = preferences.settings.first()
            }
            val needsSnapshot = preferences.isPendingHouseholdSnapshot() &&
                !preferences.isSyncSnapshotQueued(settings.householdId!!)
            if (needsSnapshot) {
                queueHouseholdSnapshot(settings.householdId!!)
            }

            val since = settings.lastSyncToken ?: 0L
            val pending = loadPendingEntities()
            val pushResult = if (pending.isNotEmpty()) {
                api.push(token, pending)
            } else {
                null
            }

            val pull = api.pull(token, since)
            applyRemoteEntities(pull.entities)
            reconcileAccountBalances()
            if (pull.household != null) {
                applyHouseholdSnapshot(pull.household)
            } else {
                syncHouseholdMembers(token, settings.householdId!!)
            }
            val nextSyncToken = if (pull.entities.isEmpty()) {
                since
            } else {
                maxOf(pull.syncToken, pull.entities.maxOf { it.updatedAt })
            }
            preferences.setLastSyncToken(nextSyncToken)
            if (pushResult != null) {
                pushResult.accepted.forEach { db.pendingSyncDao().delete(it) }
            }
            if (needsSnapshot) {
                preferences.setSyncSnapshotQueued(settings.householdId!!)
                preferences.setPendingHouseholdSnapshot(false)
            }

            SyncStatus(
                success = true,
                pushedCount = pushResult?.accepted?.size ?: 0,
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
        val householdId = preferences.settings.first().householdId ?: return
        db.savingsGoalDao().getById("household:$householdId")?.let { enqueueEntity(it.toSyncEntity(now)) }
        db.savingsLedgerDao().getAllForGoal("household:$householdId").forEach {
            enqueueEntity(it.toSyncEntity(now))
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

    private suspend fun wipeLocalBudgetForJoin() {
        db.transactionDao().deleteAll()
        db.categoryDao().deleteAll()
        db.accountDao().deleteAll()
        db.splitAllocationDao().deleteAll()
        db.iouBalanceDao().deleteAll()
        db.savingsLedgerDao().deleteAll()
        db.savingsGoalDao().deleteAll()
        db.pendingSyncDao().deleteAll()
    }

    private suspend fun applyHouseholdSnapshot(snapshot: HouseholdSnapshotDto) {
        preferences.setHouseholdId(snapshot.id)
        preferences.setHouseholdName(snapshot.name)
        val now = System.currentTimeMillis()
        val serverIds = snapshot.members.map { it.id }.toSet()
        val localMembers = db.familyMemberDao().observeAll().first()
        localMembers.filter { it.id !in serverIds && !it.id.startsWith("local") }.forEach {
            db.familyMemberDao().delete(it.id)
        }
        snapshot.members.forEach { dto ->
            db.familyMemberDao().upsert(dto.toFamilyMemberEntity(snapshot.id, now))
        }
    }

    private suspend fun syncHouseholdMembers(token: String, householdId: String?) {
        if (householdId.isNullOrBlank()) return
        val household = api.getHousehold(token) ?: error("Не удалось загрузить данные семьи")
        applyHouseholdSnapshot(
            HouseholdSnapshotDto(
                id = household.id,
                name = household.name,
                members = household.members,
            ),
        )
    }

    private fun HouseholdMemberDto.toFamilyMemberEntity(
        householdId: String,
        now: Long,
    ) = FamilyMemberEntity(
        id = id,
        householdId = householdId,
        userId = userId,
        name = displayName,
        role = role,
        visibility = visibility,
        avatar = avatar.ifBlank { memberAvatar(displayName) },
        syncVersion = 0,
        createdAt = now,
        updatedAt = now,
    )

    private fun memberAvatar(name: String): String {
        val emojis = listOf("👨", "👩", "👦", "👧", "👴", "👵", "🧑")
        return emojis[name.hashCode().mod(emojis.size).let { if (it < 0) it + emojis.size else it }]
            .ifBlank { DEFAULT_MEMBER_AVATAR }
    }

    private suspend fun applyRemoteEntities(entities: List<SyncEntityDto>) {
        entities.forEach { entity ->
            if (entity.deleted) {
                when (entity.type) {
                    "account" -> db.accountDao().delete(entity.id)
                    "category" -> db.categoryDao().delete(entity.id)
                    "transaction" -> {
                        val existing = db.transactionDao().observeById(entity.id).first()
                        if (existing != null) {
                            reverseBalanceDelta(existing.accountId, existing.type, existing.amountKopecks)
                        }
                        db.transactionDao().delete(entity.id)
                    }
                    "savings_goal" -> db.savingsGoalDao().delete(entity.id)
                    "savings_entry" -> db.savingsLedgerDao().delete(entity.id)
                    "family_member" -> db.familyMemberDao().delete(entity.id)
                }
                return@forEach
            }
            val payload = JSONObject(entity.payload)
            when (entity.type) {
                "account" -> upsertIfNewer(payload.toAccount())
                "category" -> upsertIfNewer(payload.toCategory())
                "transaction" -> upsertIfNewer(payload.toTransaction())
                "savings_goal" -> upsertIfNewer(payload.toSavingsGoal())
                "savings_entry" -> upsertIfNewer(payload.toSavingsLedger())
            }
        }
    }

    suspend fun reconcileAccountBalances() {
        val accounts = db.accountDao().observeAll().first()
        if (accounts.isEmpty()) return
        val transactions = db.transactionDao().observeAll().first()
        val now = System.currentTimeMillis()
        var changed = false
        accounts.forEach { account ->
            val computed = transactions
                .filter { it.accountId == account.id }
                .sumOf { tx -> if (tx.type == "income") tx.amountKopecks else -tx.amountKopecks }
            if (account.balanceKopecks != computed) {
                val updated = account.copy(balanceKopecks = computed, updatedAt = now)
                db.accountDao().upsert(updated)
                enqueueAccount(updated, schedule = false)
                changed = true
            }
        }
        if (changed) scheduleSync()
    }

    private suspend fun upsertIfNewer(account: AccountEntity) {
        val existing = db.accountDao().getById(account.id)
        if (existing == null || account.updatedAt >= existing.updatedAt) {
            db.accountDao().upsert(account)
        }
    }

    private suspend fun upsertIfNewer(category: CategoryEntity) {
        val existing = db.categoryDao().getById(category.id)
        if (existing == null || category.updatedAt >= existing.updatedAt) {
            db.categoryDao().upsert(category)
        }
    }

    suspend fun enqueueSavingsGoal(goal: SavingsGoalEntity, schedule: Boolean = true) {
        enqueueEntity(goal.toSyncEntity(System.currentTimeMillis()))
        if (schedule) scheduleSync()
    }

    suspend fun enqueueSavingsEntry(entry: SavingsLedgerEntity, schedule: Boolean = true) {
        enqueueEntity(entry.toSyncEntity(System.currentTimeMillis()))
        if (schedule) scheduleSync()
    }

    private suspend fun upsertIfNewer(goal: SavingsGoalEntity) {
        val existing = db.savingsGoalDao().getById(goal.id)
        if (existing == null || goal.updatedAt >= existing.updatedAt) {
            db.savingsGoalDao().upsert(goal)
        }
    }

    private suspend fun upsertIfNewer(entry: SavingsLedgerEntity) {
        val existing = db.savingsLedgerDao().getById(entry.id)
        if (existing == null || entry.updatedAt >= existing.updatedAt) {
            db.savingsLedgerDao().upsert(entry)
        }
    }

    private suspend fun upsertIfNewer(transaction: TransactionEntity) {
        val existing = db.transactionDao().observeById(transaction.id).first()
        if (existing == null || transaction.updatedAt >= existing.updatedAt) {
            db.transactionDao().upsert(transaction)
        }
    }

    private suspend fun reverseBalanceDelta(accountId: String, type: String, amountKopecks: Long) {
        val account = db.accountDao().getById(accountId) ?: return
        val delta = if (type == "income") -amountKopecks else amountKopecks
        db.accountDao().upsert(
            account.copy(
                balanceKopecks = account.balanceKopecks + delta,
                updatedAt = System.currentTimeMillis(),
            ),
        )
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
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
    )

    private fun SavingsGoalEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "savings_goal",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("householdId", householdId)
            put("goalType", goalType)
            if (customName != null) put("customName", customName)
            put("targetKopecks", targetKopecks)
            put("savedKopecks", savedKopecks)
            if (incomePercent != null) put("incomePercent", incomePercent)
            if (monthlyPlanKopecks != null) put("monthlyPlanKopecks", monthlyPlanKopecks)
            put("isActive", isActive)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
    )

    private fun SavingsLedgerEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "savings_entry",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("goalId", goalId)
            put("amountKopecks", amountKopecks)
            put("entryType", entryType)
            if (transactionId != null) put("transactionId", transactionId)
            put("dateEpochDay", dateEpochDay)
            if (note != null) put("note", note)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = 1,
        updatedAt = now,
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

    private fun JSONObject.toSavingsGoal() = SavingsGoalEntity(
        id = getString("id"),
        householdId = getString("householdId"),
        goalType = getString("goalType"),
        customName = if (has("customName") && !isNull("customName")) getString("customName") else null,
        targetKopecks = getLong("targetKopecks"),
        savedKopecks = optLong("savedKopecks", 0),
        incomePercent = if (has("incomePercent") && !isNull("incomePercent")) getInt("incomePercent") else null,
        monthlyPlanKopecks = if (has("monthlyPlanKopecks") && !isNull("monthlyPlanKopecks")) {
            getLong("monthlyPlanKopecks")
        } else null,
        isActive = optBoolean("isActive", false),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )

    private fun JSONObject.toSavingsLedger() = SavingsLedgerEntity(
        id = getString("id"),
        goalId = getString("goalId"),
        amountKopecks = getLong("amountKopecks"),
        entryType = getString("entryType"),
        transactionId = if (has("transactionId") && !isNull("transactionId")) getString("transactionId") else null,
        dateEpochDay = getLong("dateEpochDay"),
        note = if (has("note") && !isNull("note")) getString("note") else null,
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
}
