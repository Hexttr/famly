package com.famly.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String, val displayName: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class UpdateProfileRequest(val displayName: String)

@Serializable
data class ProfileResponse(val displayName: String, val email: String)

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
data class UpdateMemberRequest(
    val role: String? = null,
    val visibility: String? = null,
    val displayName: String? = null,
)

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

@Serializable
data class AdminLoginRequest(val email: String, val password: String)

@Serializable
data class AdminStatsResponse(
    val usersCount: Int,
    val householdsCount: Int,
    val syncEvents24h: Int,
    val activeSubscriptions: Int,
)

@Serializable
data class AdminUserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Long,
    val householdId: String?,
    val isAdmin: Boolean,
)

@Serializable
data class AdminHouseholdDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String,
    val createdAt: Long,
    val members: List<HouseholdMemberDto>,
)

@Serializable
data class AdminSyncLogDto(
    val id: String,
    val householdId: String,
    val entityType: String,
    val entityId: String,
    val payload: String,
    val updatedAt: Long,
    val deleted: Boolean,
)

@Serializable
data class AdminGrantSubscriptionRequest(val userId: String, val expiresAt: Long?)
