package com.famly.backend.db

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100)
    override val primaryKey = PrimaryKey(id)
}

object Households : Table("households") {
    val id = varchar("id", 36)
    val name = varchar("name", 100)
    val ownerId = varchar("owner_id", 36)
    val inviteCode = varchar("invite_code", 12).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

object HouseholdMembers : Table("household_members") {
    val id = varchar("id", 36)
    val householdId = varchar("household_id", 36)
    val userId = varchar("user_id", 36)
    val role = varchar("role", 20)
    val visibility = varchar("visibility", 20)
    override val primaryKey = PrimaryKey(id)
}

object SyncLog : Table("sync_log") {
    val id = varchar("id", 36)
    val householdId = varchar("household_id", 36)
    val entityType = varchar("entity_type", 50)
    val entityId = varchar("entity_id", 36)
    val payload = text("payload")
    val syncVersion = integer("sync_version")
    val updatedAt = long("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Subscriptions : Table("subscriptions") {
    val userId = varchar("user_id", 36)
    val isActive = bool("is_active")
    val expiresAt = long("expires_at").nullable()
    val source = varchar("source", 20)
    override val primaryKey = PrimaryKey(userId)
}
