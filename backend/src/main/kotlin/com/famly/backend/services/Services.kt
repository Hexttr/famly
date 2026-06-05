package com.famly.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.famly.backend.db.AdminAuditLog
import com.famly.backend.db.HouseholdMembers
import com.famly.backend.db.Households
import com.famly.backend.db.Subscriptions
import com.famly.backend.db.SyncLog
import com.famly.backend.db.Users
import com.famly.backend.models.AdminAuditDto
import com.famly.backend.models.AdminHouseholdDto
import com.famly.backend.models.AdminHouseholdListDto
import com.famly.backend.models.AdminStatsResponse
import com.famly.backend.models.AdminSyncLogDto
import com.famly.backend.models.AdminSyncLogSummaryDto
import com.famly.backend.models.AdminUserDto
import com.famly.backend.models.HouseholdMemberDto
import com.famly.backend.models.RegistrationsDayDto
import com.famly.backend.models.SyncEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class UserRecord(val id: String, val email: String, val passwordHash: String, val displayName: String, val isAdmin: Boolean)

class AuthService {
    private val secret = System.getenv("JWT_SECRET") ?: "famly-dev-secret-change-in-production"
    val verifier = JWT.require(Algorithm.HMAC256(secret)).build()

    private val loginAttempts = ConcurrentHashMap<String, MutableList<Long>>()
    private val maxAttempts = 10
    private val windowMs = 15 * 60 * 1000L

    fun register(email: String, password: String, displayName: String): Pair<String, String> = transaction {
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        require(displayName.isNotBlank()) { "Display name is required" }
        val normalized = email.trim().lowercase()
        val exists = Users.selectAll().where { Users.email eq normalized }.count() > 0
        if (exists) error("Email already registered")
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        Users.insert {
            it[Users.id] = id
            it[Users.email] = normalized
            it[Users.passwordHash] = hashPassword(password)
            it[Users.displayName] = displayName.trim()
            it[Users.isAdmin] = false
            it[Users.createdAt] = now
        }
        id to token(id, admin = false)
    }

    fun login(email: String, password: String): Pair<String, String> {
        checkRateLimit(email)
        val (userId, _) = authenticate(email, password)
        return userId to token(userId, admin = false)
    }

    fun adminLogin(email: String, password: String): Pair<String, String> {
        checkRateLimit("admin:${email.trim().lowercase()}")
        val (userId, isAdmin) = authenticate(email, password)
        if (!isAdmin) throw IllegalArgumentException("Admin access denied")
        return userId to adminToken(userId)
    }

    fun adminEmail(userId: String): String? = transaction {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.email)
    }

    private fun authenticate(email: String, password: String): Pair<String, Boolean> = transaction {
        require(email.isNotBlank()) { "Email is required" }
        require(password.isNotBlank()) { "Password is required" }
        val row = Users.selectAll().where { Users.email eq email.trim().lowercase() }.singleOrNull()
            ?: throw IllegalArgumentException("Invalid credentials")
        if (!verifyPassword(password, row[Users.passwordHash])) {
            throw IllegalArgumentException("Invalid credentials")
        }
        val userId = row[Users.id]
        if (!row[Users.passwordHash].startsWith("\$2")) {
            Users.update({ Users.id eq userId }) {
                it[Users.passwordHash] = hashPassword(password)
            }
        }
        userId to row[Users.isAdmin]
    }

    fun isAdmin(userId: String): Boolean = transaction {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.isAdmin) == true
    }

    fun displayName(userId: String): String = transaction {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.displayName) ?: "User"
    }

    fun updateDisplayName(userId: String, displayName: String): String = transaction {
        val trimmed = displayName.trim()
        require(trimmed.isNotBlank()) { "Display name is required" }
        require(trimmed.length <= 100) { "Display name is too long" }
        val row = Users.selectAll().where { Users.id eq userId }.singleOrNull()
            ?: throw IllegalArgumentException("User not found")
        Users.update({ Users.id eq userId }) {
            it[Users.displayName] = trimmed
        }
        HouseholdMembers.update({ HouseholdMembers.userId eq userId }) {
            it[HouseholdMembers.displayName] = trimmed
        }
        trimmed
    }

    fun profile(userId: String): Pair<String, String> = transaction {
        val row = Users.selectAll().where { Users.id eq userId }.singleOrNull()
            ?: throw IllegalArgumentException("User not found")
        row[Users.displayName] to row[Users.email]
    }

    fun token(userId: String, admin: Boolean = false): String {
        val builder = JWT.create().withClaim("userId", userId)
        if (admin) builder.withClaim("role", "admin")
        return builder.sign(Algorithm.HMAC256(secret))
    }

    fun adminToken(userId: String): String =
        JWT.create()
            .withClaim("userId", userId)
            .withClaim("role", "admin")
            .withExpiresAt(Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(secret))

    private fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

    private fun verifyPassword(password: String, storedHash: String): Boolean {
        if (storedHash.startsWith("\$2")) return BCrypt.checkpw(password, storedHash)
        return storedHash == legacySha256(password)
    }

    private fun legacySha256(password: String): String =
        MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun checkRateLimit(key: String) {
        val now = System.currentTimeMillis()
        val attempts = loginAttempts.getOrPut(key.lowercase()) { mutableListOf() }
        synchronized(attempts) {
            attempts.removeAll { now - it > windowMs }
            if (attempts.size >= maxAttempts) throw IllegalStateException("Too many login attempts")
            attempts.add(now)
        }
    }
}

class HouseholdService {
    data class HouseholdRecord(val id: String, val name: String, val ownerId: String, val inviteCode: String)
    data class MemberRecord(val id: String, val userId: String, val displayName: String, val role: String, val visibility: String)

    fun create(name: String, ownerId: String, ownerName: String): HouseholdRecord = transaction {
        val existing = getForUser(ownerId)
        if (existing != null) throw IllegalStateException("Already in a household")
        val id = UUID.randomUUID().toString()
        val code = UUID.randomUUID().toString().take(8).uppercase()
        val now = System.currentTimeMillis()
        Households.insert {
            it[Households.id] = id
            it[Households.name] = name
            it[Households.ownerId] = ownerId
            it[Households.inviteCode] = code
            it[Households.createdAt] = now
        }
        HouseholdMembers.insert {
            it[HouseholdMembers.id] = UUID.randomUUID().toString()
            it[HouseholdMembers.householdId] = id
            it[HouseholdMembers.userId] = ownerId
            it[HouseholdMembers.displayName] = ownerName
            it[HouseholdMembers.role] = "admin"
            it[HouseholdMembers.visibility] = "full"
        }
        HouseholdRecord(id, name, ownerId, code)
    }

    fun getForUser(userId: String): HouseholdRecord? = transaction {
        val memberRow = HouseholdMembers
            .innerJoin(Households, { householdId }, { Households.id })
            .selectAll()
            .where { HouseholdMembers.userId eq userId }
            .singleOrNull() ?: return@transaction null
        HouseholdRecord(
            memberRow[Households.id],
            memberRow[Households.name],
            memberRow[Households.ownerId],
            memberRow[Households.inviteCode],
        )
    }

    fun members(householdId: String): List<MemberRecord> = transaction {
        HouseholdMembers.selectAll().where { HouseholdMembers.householdId eq householdId }.map { row ->
            MemberRecord(
                row[HouseholdMembers.id],
                row[HouseholdMembers.userId],
                row[HouseholdMembers.displayName],
                row[HouseholdMembers.role],
                row[HouseholdMembers.visibility],
            )
        }
    }

    fun memberForUser(householdId: String, userId: String): MemberRecord? = transaction {
        HouseholdMembers.selectAll()
            .where { (HouseholdMembers.householdId eq householdId) and (HouseholdMembers.userId eq userId) }
            .singleOrNull()
            ?.let { row ->
                MemberRecord(
                    row[HouseholdMembers.id],
                    row[HouseholdMembers.userId],
                    row[HouseholdMembers.displayName],
                    row[HouseholdMembers.role],
                    row[HouseholdMembers.visibility],
                )
            }
    }

    fun join(inviteCode: String, userId: String, displayName: String): HouseholdRecord = transaction {
        val existingHousehold = getForUser(userId)
        val household = Households.selectAll().where { Households.inviteCode eq inviteCode.trim().uppercase() }
            .singleOrNull()
            ?: throw IllegalArgumentException("Invalid invite")
        val householdId = household[Households.id]
        if (existingHousehold != null && existingHousehold.id != householdId) {
            throw IllegalStateException("Leave current household before joining another")
        }
        val alreadyMember = HouseholdMembers.selectAll()
            .where { (HouseholdMembers.householdId eq householdId) and (HouseholdMembers.userId eq userId) }
            .count() > 0
        if (!alreadyMember) {
            HouseholdMembers.insert {
                it[HouseholdMembers.id] = UUID.randomUUID().toString()
                it[HouseholdMembers.householdId] = householdId
                it[HouseholdMembers.userId] = userId
                it[HouseholdMembers.displayName] = displayName
                it[HouseholdMembers.role] = "member"
                it[HouseholdMembers.visibility] = "partial"
            }
        }
        HouseholdRecord(
            householdId,
            household[Households.name],
            household[Households.ownerId],
            household[Households.inviteCode],
        )
    }

    fun leave(userId: String) = transaction {
        val household = getForUser(userId) ?: throw IllegalArgumentException("Not in a household")
        val member = memberForUser(household.id, userId) ?: throw IllegalArgumentException("Not a member")
        val allMembers = members(household.id)
        if (household.ownerId == userId && allMembers.size > 1) {
            val successor = allMembers.firstOrNull { it.userId != userId && it.role == "admin" }
                ?: allMembers.first { it.userId != userId }
            Households.update({ Households.id eq household.id }) {
                it[ownerId] = successor.userId
            }
            HouseholdMembers.update({ HouseholdMembers.id eq successor.id }) {
                it[role] = "admin"
            }
        }
        HouseholdMembers.deleteWhere { HouseholdMembers.id eq member.id }
        if (members(household.id).isEmpty()) {
            SyncLog.deleteWhere { SyncLog.householdId eq household.id }
            Households.deleteWhere { Households.id eq household.id }
        }
    }

    fun updateMember(
        householdId: String,
        memberId: String,
        actorUserId: String,
        role: String?,
        visibility: String?,
        displayName: String?,
    ): MemberRecord = transaction {
        val actor = memberForUser(householdId, actorUserId)
            ?: throw AccessDeniedException("Not a member")
        val row = HouseholdMembers.selectAll()
            .where { (HouseholdMembers.id eq memberId) and (HouseholdMembers.householdId eq householdId) }
            .singleOrNull() ?: throw IllegalArgumentException("Member not found")
        val targetUserId = row[HouseholdMembers.userId]
        val isSelf = targetUserId == actorUserId
        val isAdmin = actor.role == "admin"

        when {
            isAdmin -> {
                if (role != null && role != "admin" && isSelf) {
                    val adminCount = members(householdId).count { it.role == "admin" }
                    if (adminCount <= 1) throw IllegalStateException("Cannot demote the last admin")
                }
            }
            isSelf -> {
                if (role != null && role != row[HouseholdMembers.role]) {
                    throw AccessDeniedException("Admin role required")
                }
            }
            else -> throw AccessDeniedException("Admin role required")
        }

        HouseholdMembers.update({ HouseholdMembers.id eq memberId }) {
            if (isAdmin) {
                role?.let { value -> it[HouseholdMembers.role] = value }
            }
            if (isAdmin || isSelf) {
                visibility?.let { value -> it[HouseholdMembers.visibility] = value }
                displayName?.let { value -> it[HouseholdMembers.displayName] = value.trim() }
            }
        }
        val updated = HouseholdMembers.selectAll().where { HouseholdMembers.id eq memberId }.single()
        MemberRecord(
            updated[HouseholdMembers.id],
            updated[HouseholdMembers.userId],
            updated[HouseholdMembers.displayName],
            updated[HouseholdMembers.role],
            updated[HouseholdMembers.visibility],
        )
    }

    fun removeMember(householdId: String, memberId: String, actorUserId: String) = transaction {
        val actor = memberForUser(householdId, actorUserId)
            ?: throw IllegalArgumentException("Not a member")
        if (actor.role != "admin") throw IllegalArgumentException("Admin role required")
        val target = HouseholdMembers.selectAll()
            .where { (HouseholdMembers.id eq memberId) and (HouseholdMembers.householdId eq householdId) }
            .singleOrNull() ?: throw IllegalArgumentException("Member not found")
        if (target[HouseholdMembers.userId] == actorUserId) {
            throw IllegalArgumentException("Use leave endpoint to remove yourself")
        }
        HouseholdMembers.deleteWhere { HouseholdMembers.id eq memberId }
    }

    fun regenerateInvite(householdId: String, userId: String): String = transaction {
        val member = memberForUser(householdId, userId)
            ?: throw IllegalArgumentException("Not a member")
        if (member.role != "admin") throw IllegalArgumentException("Admin role required")
        regenerateInviteCode(householdId)
    }

    fun regenerateInviteCode(householdId: String): String = transaction {
        val code = UUID.randomUUID().toString().take(8).uppercase()
        Households.update({ Households.id eq householdId }) {
            it[inviteCode] = code
        }
        code
    }
}

class SubscriptionService {
    data class SubscriptionRecord(val userId: String, val isActive: Boolean, val expiresAt: Long?, val source: String)

    fun activate(userId: String, source: String, expiresAt: Long?) = transaction {
        val existing = Subscriptions.selectAll().where { Subscriptions.userId eq userId }.singleOrNull()
        if (existing == null) {
            Subscriptions.insert {
                it[Subscriptions.userId] = userId
                it[Subscriptions.isActive] = expiresAt != null
                it[Subscriptions.expiresAt] = expiresAt
                it[Subscriptions.paymentProvider] = source
            }
        } else {
            Subscriptions.update({ Subscriptions.userId eq userId }) {
                it[Subscriptions.isActive] = expiresAt != null
                it[Subscriptions.expiresAt] = expiresAt
                it[Subscriptions.paymentProvider] = source
            }
        }
    }

    fun status(userId: String): SubscriptionRecord = transaction {
        val row = Subscriptions.selectAll().where { Subscriptions.userId eq userId }.singleOrNull()
            ?: return@transaction SubscriptionRecord(userId, false, null, "")
        val expiresAt = row[Subscriptions.expiresAt]
        val active = row[Subscriptions.isActive] && (expiresAt == null || expiresAt > System.currentTimeMillis())
        SubscriptionRecord(userId, active, expiresAt, row[Subscriptions.paymentProvider])
    }
}

class SyncService {
    fun push(householdId: String, entities: List<SyncEntity>) = transaction {
        entities.forEach { entity ->
            if (entity.type == "family_member") return@forEach
            val compositeId = "${entity.type}:${entity.id}"
            val existing = SyncLog.selectAll().where { SyncLog.id eq compositeId }.singleOrNull()
            if (existing == null) {
                SyncLog.insert {
                    it[SyncLog.id] = compositeId
                    it[SyncLog.householdId] = householdId
                    it[SyncLog.entityType] = entity.type
                    it[SyncLog.entityId] = entity.id
                    it[SyncLog.payload] = entity.payload
                    it[SyncLog.syncVersion] = entity.syncVersion
                    it[SyncLog.updatedAt] = entity.updatedAt
                    it[SyncLog.deleted] = entity.deleted
                }
            } else if (entity.updatedAt >= existing[SyncLog.updatedAt]) {
                SyncLog.update({ SyncLog.id eq compositeId }) {
                    it[SyncLog.payload] = entity.payload
                    it[SyncLog.syncVersion] = entity.syncVersion
                    it[SyncLog.updatedAt] = entity.updatedAt
                    it[SyncLog.deleted] = entity.deleted
                }
            }
        }
    }

    fun pull(householdId: String, since: Long): Pair<List<SyncEntity>, Long> = transaction {
        val entities = SyncLog.selectAll()
            .where { (SyncLog.householdId eq householdId) and (SyncLog.updatedAt greater since) }
            .map { row ->
                SyncEntity(
                    type = row[SyncLog.entityType],
                    id = row[SyncLog.entityId],
                    payload = row[SyncLog.payload],
                    syncVersion = row[SyncLog.syncVersion],
                    updatedAt = row[SyncLog.updatedAt],
                    deleted = row[SyncLog.deleted],
                )
            }
        entities to System.currentTimeMillis()
    }
}

class AdminService {
    private data class CachedStats(val stats: AdminStatsResponse, val at: Long)
    private val statsCache = AtomicReference<CachedStats?>(null)
    private val statsTtlMs = 60_000L

    fun clampPageSize(requested: Int): Int = requested.coerceIn(1, 50)

    fun stats(): AdminStatsResponse = transaction {
        val now = System.currentTimeMillis()
        val dayAgo = now - 24 * 60 * 60 * 1000
        AdminStatsResponse(
            usersCount = Users.selectAll().count().toInt(),
            householdsCount = Households.selectAll().count().toInt(),
            syncEvents24h = SyncLog.selectAll().where { SyncLog.updatedAt greater dayAgo }.count().toInt(),
            activeSubscriptions = Subscriptions.selectAll().where { Subscriptions.isActive eq true }.count().toInt(),
        )
    }

    fun statsCached(): AdminStatsResponse {
        val now = System.currentTimeMillis()
        statsCache.get()?.let { if (now - it.at < statsTtlMs) return it.stats }
        val fresh = stats()
        statsCache.set(CachedStats(fresh, now))
        return fresh
    }

    fun usersCount(query: String? = null): Int = transaction {
        val q = query?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        if (q == null) {
            Users.selectAll().count().toInt()
        } else {
            Users.selectAll().where { Users.email like "%$q%" }.count().toInt()
        }
    }

    fun listUsers(page: Int = 1, pageSize: Int = 25, query: String? = null): List<AdminUserDto> = transaction {
        val size = clampPageSize(pageSize)
        val offset = ((page.coerceAtLeast(1) - 1) * size).toLong()
        val q = query?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val rows = if (q == null) {
            Users.selectAll().orderBy(Users.createdAt to SortOrder.DESC).limit(size, offset)
        } else {
            Users.selectAll()
                .where { Users.email like "%$q%" }
                .orderBy(Users.createdAt to SortOrder.DESC)
                .limit(size, offset)
        }.toList()
        val userIds = rows.map { it[Users.id] }
        val householdByUser = if (userIds.isEmpty()) {
            emptyMap()
        } else {
            HouseholdMembers.selectAll()
                .where { HouseholdMembers.userId inList userIds }
                .associate { it[HouseholdMembers.userId] to it[HouseholdMembers.householdId] }
        }
        rows.map { row ->
            val userId = row[Users.id]
            AdminUserDto(
                id = userId,
                email = row[Users.email],
                displayName = row[Users.displayName],
                createdAt = row[Users.createdAt],
                householdId = householdByUser[userId],
                isAdmin = row[Users.isAdmin],
            )
        }
    }

    fun listUsers(): List<AdminUserDto> = listUsers(page = 1, pageSize = 1000)

    fun householdsCount(): Int = transaction { Households.selectAll().count().toInt() }

    fun listHouseholdsPage(page: Int = 1, pageSize: Int = 25): List<AdminHouseholdListDto> = transaction {
        val size = clampPageSize(pageSize)
        val offset = ((page.coerceAtLeast(1) - 1) * size).toLong()
        val rows = Households.selectAll()
            .orderBy(Households.createdAt to SortOrder.DESC)
            .limit(size, offset)
            .toList()
        val ids = rows.map { it[Households.id] }
        val memberCounts = if (ids.isEmpty()) {
            emptyMap()
        } else {
            HouseholdMembers.selectAll()
                .where { HouseholdMembers.householdId inList ids }
                .toList()
                .groupBy { it[HouseholdMembers.householdId] }
                .mapValues { it.value.size }
        }
        rows.map { row ->
            val id = row[Households.id]
            AdminHouseholdListDto(
                id = id,
                name = row[Households.name],
                ownerId = row[Households.ownerId],
                inviteCode = row[Households.inviteCode],
                createdAt = row[Households.createdAt],
                memberCount = memberCounts[id] ?: 0,
            )
        }
    }

    fun getHousehold(id: String): AdminHouseholdDto? = transaction {
        val row = Households.selectAll().where { Households.id eq id }.singleOrNull() ?: return@transaction null
        AdminHouseholdDto(
            id = id,
            name = row[Households.name],
            ownerId = row[Households.ownerId],
            inviteCode = row[Households.inviteCode],
            createdAt = row[Households.createdAt],
            members = HouseholdMembers.selectAll().where { HouseholdMembers.householdId eq id }.map { m ->
                HouseholdMemberDto(
                    m[HouseholdMembers.id],
                    m[HouseholdMembers.userId],
                    m[HouseholdMembers.displayName],
                    m[HouseholdMembers.role],
                    m[HouseholdMembers.visibility],
                )
            },
        )
    }

    fun listHouseholds(): List<AdminHouseholdDto> = transaction {
        Households.selectAll().orderBy(Households.createdAt to SortOrder.DESC).map { row ->
            val id = row[Households.id]
            AdminHouseholdDto(
                id = id,
                name = row[Households.name],
                ownerId = row[Households.ownerId],
                inviteCode = row[Households.inviteCode],
                createdAt = row[Households.createdAt],
                members = HouseholdMembers.selectAll().where { HouseholdMembers.householdId eq id }.map { m ->
                    HouseholdMemberDto(
                        m[HouseholdMembers.id],
                        m[HouseholdMembers.userId],
                        m[HouseholdMembers.displayName],
                        m[HouseholdMembers.role],
                        m[HouseholdMembers.visibility],
                    )
                },
            )
        }
    }

    fun syncLogSummary(page: Int = 1, pageSize: Int = 25, entityType: String? = null): List<AdminSyncLogSummaryDto> =
        transaction {
            val size = clampPageSize(pageSize)
            val offset = ((page.coerceAtLeast(1) - 1) * size).toLong()
            val baseQuery = SyncLog.selectAll().let { query ->
                if (entityType != null) query.where { SyncLog.entityType eq entityType } else query
            }
            baseQuery.orderBy(SyncLog.updatedAt to SortOrder.DESC).limit(size, offset).map { row ->
                AdminSyncLogSummaryDto(
                    row[SyncLog.id],
                    row[SyncLog.householdId],
                    row[SyncLog.entityType],
                    row[SyncLog.entityId],
                    row[SyncLog.updatedAt],
                    row[SyncLog.deleted],
                )
            }
        }

    fun syncLogCount(entityType: String? = null): Int = transaction {
        if (entityType != null) {
            SyncLog.selectAll().where { SyncLog.entityType eq entityType }.count().toInt()
        } else {
            SyncLog.selectAll().count().toInt()
        }
    }

    fun syncLogEntry(id: String, maxPayloadChars: Int = 2048): AdminSyncLogDto? = transaction {
        val row = SyncLog.selectAll().where { SyncLog.id eq id }.singleOrNull() ?: return@transaction null
        val payload = row[SyncLog.payload]
        val truncated = if (payload.length > maxPayloadChars) payload.take(maxPayloadChars) + "…" else payload
        AdminSyncLogDto(
            row[SyncLog.id],
            row[SyncLog.householdId],
            row[SyncLog.entityType],
            row[SyncLog.entityId],
            truncated,
            row[SyncLog.updatedAt],
            row[SyncLog.deleted],
        )
    }

    fun syncLog(limit: Int = 100, entityType: String? = null): List<AdminSyncLogDto> = transaction {
        val baseQuery = SyncLog.selectAll().let { query ->
            if (entityType != null) query.where { SyncLog.entityType eq entityType } else query
        }
        baseQuery.orderBy(SyncLog.updatedAt to SortOrder.DESC).limit(limit).map { row ->
            AdminSyncLogDto(
                row[SyncLog.id],
                row[SyncLog.householdId],
                row[SyncLog.entityType],
                row[SyncLog.entityId],
                row[SyncLog.payload],
                row[SyncLog.updatedAt],
                row[SyncLog.deleted],
            )
        }
    }

    fun transactions(householdId: String?, limit: Int = 200): List<AdminSyncLogDto> = transaction {
        val baseQuery = SyncLog.selectAll()
            .where { SyncLog.entityType eq "transaction" }
            .let { query ->
                if (householdId != null) query.andWhere { SyncLog.householdId eq householdId } else query
            }
        baseQuery.orderBy(SyncLog.updatedAt to SortOrder.DESC).limit(limit).map { row ->
            AdminSyncLogDto(
                row[SyncLog.id],
                row[SyncLog.householdId],
                row[SyncLog.entityType],
                row[SyncLog.entityId],
                row[SyncLog.payload],
                row[SyncLog.updatedAt],
                row[SyncLog.deleted],
            )
        }
    }

    fun transactionSummaries(householdId: String, limit: Int = 20): List<AdminSyncLogSummaryDto> = transaction {
        SyncLog.selectAll()
            .where { (SyncLog.householdId eq householdId) and (SyncLog.entityType eq "transaction") }
            .orderBy(SyncLog.updatedAt to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                AdminSyncLogSummaryDto(
                    row[SyncLog.id],
                    row[SyncLog.householdId],
                    row[SyncLog.entityType],
                    row[SyncLog.entityId],
                    row[SyncLog.updatedAt],
                    row[SyncLog.deleted],
                )
            }
    }

    fun listAudit(page: Int = 1, pageSize: Int = 25): List<AdminAuditDto> = transaction {
        val size = clampPageSize(pageSize)
        val offset = ((page.coerceAtLeast(1) - 1) * size).toLong()
        val rows = AdminAuditLog.selectAll()
            .orderBy(AdminAuditLog.createdAt to SortOrder.DESC)
            .limit(size, offset)
            .toList()
        val adminIds = rows.map { it[AdminAuditLog.adminUserId] }.distinct()
        val emails = if (adminIds.isEmpty()) {
            emptyMap()
        } else {
            Users.selectAll().where { Users.id inList adminIds }.associate {
                it[Users.id] to it[Users.email]
            }
        }
        rows.map { row ->
            AdminAuditDto(
                id = row[AdminAuditLog.id],
                adminUserId = row[AdminAuditLog.adminUserId],
                adminEmail = emails[row[AdminAuditLog.adminUserId]],
                action = row[AdminAuditLog.action],
                targetType = row[AdminAuditLog.targetType],
                targetId = row[AdminAuditLog.targetId],
                details = row[AdminAuditLog.details],
                createdAt = row[AdminAuditLog.createdAt],
            )
        }
    }

    fun auditCount(): Int = transaction { AdminAuditLog.selectAll().count().toInt() }

    fun registrationsLast7Days(): List<RegistrationsDayDto> = transaction {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startMs = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val counts = Users.selectAll()
            .where { Users.createdAt greaterEq startMs }
            .map { Instant.ofEpochMilli(it[Users.createdAt]).atZone(zone).toLocalDate() }
            .groupingBy { it }
            .eachCount()
        (0..6).map { offset ->
            val day = today.minusDays(6L - offset)
            RegistrationsDayDto(day.toString(), counts[day] ?: 0)
        }
    }

    fun audit(adminUserId: String, action: String, targetType: String? = null, targetId: String? = null, details: String? = null) =
        transaction {
            AdminAuditLog.insert {
                it[AdminAuditLog.id] = UUID.randomUUID().toString()
                it[AdminAuditLog.adminUserId] = adminUserId
                it[AdminAuditLog.action] = action
                it[AdminAuditLog.targetType] = targetType
                it[AdminAuditLog.targetId] = targetId
                it[AdminAuditLog.details] = details
                it[AdminAuditLog.createdAt] = System.currentTimeMillis()
            }
        }
}

fun seedAdminUser(authService: AuthService) {
    val adminEmail = System.getenv("ADMIN_EMAIL") ?: return
    val adminPassword = System.getenv("ADMIN_PASSWORD") ?: return
    transaction {
        val existing = Users.selectAll().where { Users.email eq adminEmail.trim().lowercase() }.singleOrNull()
        if (existing == null) {
            val id = UUID.randomUUID().toString()
            Users.insert {
                it[Users.id] = id
                it[Users.email] = adminEmail.trim().lowercase()
                it[Users.passwordHash] = BCrypt.hashpw(adminPassword, BCrypt.gensalt(12))
                it[Users.displayName] = "Admin"
                it[Users.isAdmin] = true
                it[Users.createdAt] = System.currentTimeMillis()
            }
        } else if (!existing[Users.isAdmin]) {
            Users.update({ Users.id eq existing[Users.id] }) {
                it[Users.isAdmin] = true
            }
        }
    }
}
