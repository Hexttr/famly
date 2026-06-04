package com.famly.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val compositeKey: String,
    val type: String,
    val entityId: String,
    val payload: String,
    val syncVersion: Int,
    val updatedAt: Long,
    val deleted: Boolean = false,
)
