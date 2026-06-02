package com.famly.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val balanceKopecks: Long,
    val color: String,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val color: String,
    val budgetLimitKopecks: Long?,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amountKopecks: Long,
    val type: String,
    val categoryId: String,
    val accountId: String,
    val dateEpochDay: Long,
    val note: String?,
    val isRecurring: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
