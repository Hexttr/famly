package com.famly.app.data.sync

import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.remote.FamlyApiClient
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
 * Offline-first sync: queues local changes, pushes when online, pulls remote updates.
 */
class SyncRepository(
    private val api: FamlyApiClient,
    private val db: FamlyDatabase,
    private val preferences: UserPreferences,
) {
    private val pendingQueue = mutableListOf<SyncEntityDto>()

    suspend fun register(email: String, password: String, displayName: String): SyncStatus =
        runCatching {
            val result = api.register(email, password, displayName)
            preferences.setAuthSession(result.token, result.userId)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun login(email: String, password: String): SyncStatus =
        runCatching {
            val result = api.login(email, password)
            preferences.setAuthSession(result.token, result.userId)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun createHousehold(name: String): SyncStatus =
        runCatching {
            val token = requireAuthToken()
            val household = api.createHousehold(token, name)
            preferences.setHouseholdId(household.id)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    suspend fun joinHousehold(inviteCode: String): SyncStatus =
        runCatching {
            val token = requireAuthToken()
            val household = api.joinHousehold(token, inviteCode)
            preferences.setHouseholdId(household.id)
            SyncStatus(success = true)
        }.getOrElse { SyncStatus(success = false, error = it.message) }

    fun enqueue(entity: SyncEntityDto) {
        pendingQueue.removeAll { it.type == entity.type && it.id == entity.id }
        pendingQueue.add(entity)
    }

    fun enqueueTransaction(tx: TransactionEntity) =
        enqueue(tx.toSyncEntity(System.currentTimeMillis()))

    fun enqueueAccount(account: AccountEntity) =
        enqueue(account.toSyncEntity(System.currentTimeMillis()))

    fun enqueueCategory(category: CategoryEntity) =
        enqueue(category.toSyncEntity(System.currentTimeMillis()))

    fun enqueueFamilyMember(member: FamilyMemberEntity) =
        enqueue(member.toSyncEntity(System.currentTimeMillis()))

    fun enqueueDeleted(type: String, id: String) =
        enqueue(
            SyncEntityDto(
                type = type,
                id = id,
                payload = "{}",
                syncVersion = 1,
                updatedAt = System.currentTimeMillis(),
                deleted = true,
            ),
        )

    suspend fun generateInviteCode(): String {
        val token = preferences.settings.first().authToken
        val householdId = preferences.settings.first().householdId
        if (!token.isNullOrBlank() && !householdId.isNullOrBlank()) {
            runCatching { api.generateInvite(token, householdId) }.getOrNull()?.let { return it }
        }
        return "FAMLY-${UUID.randomUUID().toString().take(6).uppercase()}"
    }

    suspend fun sync(): SyncStatus {
        val token = preferences.settings.first().authToken
            ?: return SyncStatus(success = false, error = "Not authenticated")

        return runCatching {
            val since = preferences.settings.first().lastSyncToken ?: 0L
            val toPush = buildPushPayload() + pendingQueue.toList()
            val pushed = if (toPush.isNotEmpty()) api.push(token, toPush) else true
            if (!pushed) return SyncStatus(success = false, error = "Push failed")

            val pull = api.pull(token, since)
            applyRemoteEntities(pull.entities)
            preferences.setLastSyncToken(pull.syncToken)
            pendingQueue.clear()

            SyncStatus(
                success = true,
                pushedCount = toPush.size,
                pulledCount = pull.entities.size,
            )
        }.getOrElse { SyncStatus(success = false, error = it.message) }
    }

    private suspend fun requireAuthToken(): String {
        val token = preferences.settings.first().authToken
        require(!token.isNullOrBlank()) { "Not authenticated" }
        return token
    }

    private suspend fun buildPushPayload(): List<SyncEntityDto> {
        val now = System.currentTimeMillis()
        val accounts = db.accountDao().observeAll().first().map { it.toSyncEntity(now) }
        val categories = db.categoryDao().observeAll().first().map { it.toSyncEntity(now) }
        val transactions = db.transactionDao().observeAll().first().map { it.toSyncEntity(now) }
        val members = db.familyMemberDao().observeAll().first().map { it.toSyncEntity(now) }
        return accounts + categories + transactions + members
    }

    private suspend fun applyRemoteEntities(entities: List<SyncEntityDto>) {
        entities.forEach { entity ->
            if (entity.deleted) {
                when (entity.type) {
                    "account" -> db.accountDao().delete(entity.id)
                    "category" -> db.categoryDao().delete(entity.id)
                    "transaction" -> db.transactionDao().delete(entity.id)
                    "family_member" -> db.familyMemberDao().delete(entity.id)
                }
                return@forEach
            }
            val payload = JSONObject(entity.payload)
            when (entity.type) {
                "account" -> db.accountDao().upsert(payload.toAccount())
                "category" -> db.categoryDao().upsert(payload.toCategory())
                "transaction" -> db.transactionDao().upsert(payload.toTransaction())
                "family_member" -> db.familyMemberDao().upsert(payload.toFamilyMember())
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

    private fun FamilyMemberEntity.toSyncEntity(now: Long) = SyncEntityDto(
        type = "family_member",
        id = id,
        payload = JSONObject().apply {
            put("id", id)
            put("householdId", householdId)
            put("name", name)
            put("role", role)
            put("visibility", visibility)
            put("avatar", avatar)
            put("syncVersion", syncVersion)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }.toString(),
        syncVersion = syncVersion.toInt(),
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
        name = getString("name"),
        role = getString("role"),
        visibility = getString("visibility"),
        avatar = getString("avatar"),
        syncVersion = optLong("syncVersion", 0),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
    )
}
