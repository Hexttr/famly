package com.famly.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String, val displayName: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val userId: String)

@Serializable
data class HouseholdResponse(
    val id: String,
    val name: String,
    val ownerId: String,
    val members: List<HouseholdMemberDto>,
)

@Serializable
data class HouseholdMemberDto(
    val id: String,
    val userId: String,
    val displayName: String,
    val role: String,
    val visibility: String,
)

@Serializable
data class CreateHouseholdRequest(val name: String)

@Serializable
data class InviteResponse(val inviteCode: String, val inviteUrl: String)

@Serializable
data class JoinHouseholdRequest(val inviteCode: String)

@Serializable
data class SyncPushRequest(val entities: List<SyncEntity>)

@Serializable
data class SyncEntity(
    val type: String,
    val id: String,
    val payload: String,
    val syncVersion: Int,
    val updatedAt: Long,
    val deleted: Boolean = false,
)

@Serializable
data class SyncPullResponse(val entities: List<SyncEntity>, val syncToken: Long)

@Serializable
data class SubscriptionStatusResponse(
    val isPremium: Boolean,
    val expiresAt: Long?,
    val source: String?,
)

@Serializable
data class RuStoreWebhookPayload(val userId: String, val productId: String, val event: String, val expiresAt: Long?)

@Serializable
data class YooKassaWebhookPayload(val event: String, val objectId: String, val userId: String?)
