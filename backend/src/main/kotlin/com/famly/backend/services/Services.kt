package com.famly.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.famly.backend.db.HouseholdMembers
import com.famly.backend.db.Households
import com.famly.backend.db.Subscriptions
import com.famly.backend.db.SyncLog
import com.famly.backend.db.Users
import com.famly.backend.models.SyncEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.UUID

data class UserRecord(val id: String, val email: String, val passwordHash: String, val displayName: String)

class AuthService {
    private val secret = System.getenv("JWT_SECRET") ?: "famly-dev-secret-change-in-production"
    val verifier = JWT.require(Algorithm.HMAC256(secret)).build()

    fun register(email: String, password: String, displayName: String): Pair<String, String> = transaction {
        val exists = Users.selectAll().where { Users.email eq email }.count() > 0
        if (exists) error("Email already registered")
        val id = UUID.randomUUID().toString()
        Users.insert {
            it[Users.id] = id
            it[Users.email] = email
            it[Users.passwordHash] = hash(password)
            it[Users.displayName] = displayName
        }
        id to token(id)
    }

    fun login(email: String, password: String): Pair<String, String> = transaction {
        val row = Users.selectAll().where { Users.email eq email }.singleOrNull()
            ?: error("Invalid credentials")
        if (row[Users.passwordHash] != hash(password)) error("Invalid credentials")
        row[Users.id] to token(row[Users.id])
    }

    fun displayName(userId: String): String = transaction {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.displayName) ?: "User"
    }

    private fun token(userId: String): String =
        JWT.create().withClaim("userId", userId).sign(Algorithm.HMAC256(secret))

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
}

class HouseholdService {
    data class HouseholdRecord(val id: String, val name: String, val ownerId: String, val inviteCode: String)
    data class MemberRecord(val id: String, val userId: String, val displayName: String, val role: String, val visibility: String)

    fun create(name: String, ownerId: String, ownerName: String): HouseholdRecord = transaction {
        val id = UUID.randomUUID().toString()
        val code = UUID.randomUUID().toString().take(8).uppercase()
        Households.insert {
            it[Households.id] = id
            it[Households.name] = name
            it[Households.ownerId] = ownerId
            it[Households.inviteCode] = code
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

    fun join(inviteCode: String, userId: String, displayName: String): HouseholdRecord = transaction {
        val household = Households.selectAll().where { Households.inviteCode eq inviteCode }.singleOrNull()
            ?: error("Invalid invite")
        val householdId = household[Households.id]
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
            } else {
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
