package com.famly.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.famly.backend.db.AdminAuditLog
import com.famly.backend.db.HouseholdMembers
import com.famly.backend.db.Households
import com.famly.backend.db.Subscriptions
import com.famly.backend.db.SyncLog
import com.famly.backend.db.Users
import com.famly.backend.models.AdminHouseholdDto
import com.famly.backend.models.AdminStatsResponse
import com.famly.backend.models.AdminSyncLogDto
import com.famly.backend.models.AdminUserDto
import com.famly.backend.models.HouseholdMemberDto
import com.famly.backend.models.SyncEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
        return transaction {
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
            userId to token(userId, admin = row[Users.isAdmin])
        }
    }

    fun adminLogin(email: String, password: String): Pair<String, String> {
        val (userId, token) = login(email, password)
        val isAdmin = transaction {
            Users.selectAll().where { Users.id eq userId }.single()[Users.isAdmin]
        }
        if (!isAdmin) throw IllegalArgumentException("Admin access denied")
        return userId to token(userId, admin = true)
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

    fun listUsers(): List<AdminUserDto> = transaction {
        Users.selectAll().orderBy(Users.createdAt to SortOrder.DESC).map { row ->
            val userId = row[Users.id]
            val householdId = HouseholdMembers.selectAll()
                .where { HouseholdMembers.userId eq userId }
                .singleOrNull()
                ?.get(HouseholdMembers.householdId)
            AdminUserDto(
                id = userId,
                email = row[Users.email],
                displayName = row[Users.displayName],
                createdAt = row[Users.createdAt],
                householdId = householdId,
                isAdmin = row[Users.isAdmin],
            )
        }
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
