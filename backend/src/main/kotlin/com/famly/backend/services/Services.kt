package com.famly.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class UserRecord(val id: String, val email: String, val passwordHash: String, val displayName: String)

class AuthService {
    private val users = ConcurrentHashMap<String, UserRecord>()
    private val secret = System.getenv("JWT_SECRET") ?: "famly-dev-secret-change-in-production"
    val verifier = JWT.require(Algorithm.HMAC256(secret)).build()

    fun register(email: String, password: String, displayName: String): Pair<String, String> {
        if (users.values.any { it.email == email }) error("Email already registered")
        val id = UUID.randomUUID().toString()
        val user = UserRecord(id, email, hash(password), displayName)
        users[id] = user
        return id to token(id)
    }

    fun login(email: String, password: String): Pair<String, String> {
        val user = users.values.find { it.email == email && it.passwordHash == hash(password) }
            ?: error("Invalid credentials")
        return user.id to token(user.id)
    }

    private fun token(userId: String): String =
        JWT.create().withClaim("userId", userId).sign(Algorithm.HMAC256(secret))

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
}

class HouseholdService {
    private val households = ConcurrentHashMap<String, HouseholdRecord>()
    private val members = ConcurrentHashMap<String, MutableList<MemberRecord>>()

    data class HouseholdRecord(val id: String, val name: String, val ownerId: String, val inviteCode: String)
    data class MemberRecord(val id: String, val userId: String, val displayName: String, val role: String, val visibility: String)

    fun create(name: String, ownerId: String, ownerName: String): HouseholdRecord {
        val id = UUID.randomUUID().toString()
        val code = UUID.randomUUID().toString().take(8).uppercase()
        val h = HouseholdRecord(id, name, ownerId, code)
        households[id] = h
        members[id] = mutableListOf(MemberRecord(UUID.randomUUID().toString(), ownerId, ownerName, "admin", "full"))
        return h
    }

    fun getForUser(userId: String): HouseholdRecord? =
        households.values.find { h -> members[h.id]?.any { it.userId == userId } == true }

    fun members(householdId: String) = members[householdId]?.toList() ?: emptyList()

    fun join(inviteCode: String, userId: String, displayName: String): HouseholdRecord {
        val h = households.values.find { it.inviteCode == inviteCode } ?: error("Invalid invite")
        members.getOrPut(h.id) { mutableListOf() }.add(
            MemberRecord(UUID.randomUUID().toString(), userId, displayName, "member", "partial"),
        )
        return h
    }
}

class SubscriptionService {
    private val subs = ConcurrentHashMap<String, SubscriptionRecord>()
    data class SubscriptionRecord(val userId: String, val isActive: Boolean, val expiresAt: Long?, val source: String)

    fun activate(userId: String, source: String, expiresAt: Long?) {
        subs[userId] = SubscriptionRecord(userId, true, expiresAt, source)
    }

    fun status(userId: String) = subs[userId] ?: SubscriptionRecord(userId, false, null, "")
}

class SyncService {
    private val store = ConcurrentHashMap<String, MutableList<com.famly.backend.models.SyncEntity>>()

    fun push(householdId: String, entities: List<com.famly.backend.models.SyncEntity>) {
        store.getOrPut(householdId) { mutableListOf() }.addAll(entities)
    }

    fun pull(householdId: String, since: Long): Pair<List<com.famly.backend.models.SyncEntity>, Long> {
        val all = store[householdId]?.filter { it.updatedAt > since } ?: emptyList()
        val token = System.currentTimeMillis()
        return all to token
    }
}
