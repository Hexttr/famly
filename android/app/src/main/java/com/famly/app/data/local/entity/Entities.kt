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
    val rolloverKopecks: Long = 0,
    val rolloverEnabled: Boolean = false,
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
    val recurringDay: Int? = null,
    val lastRecurrenceEpochDay: Long? = null,
    val isPrivate: Boolean = false,
    val splitMemberIds: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val userId: String? = null,
    val name: String,
    val role: String,
    val visibility: String,
    val avatar: String,
    val syncVersion: Long = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "iou_balances")
data class IouBalanceEntity(
    @PrimaryKey val id: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amountKopecks: Long,
    val settledAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "split_allocations")
data class SplitAllocationEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val memberId: String,
    val shareKopecks: Long,
    val createdAt: Long,
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val goalType: String,
    val customName: String? = null,
    val targetKopecks: Long,
    val savedKopecks: Long = 0,
    val incomePercent: Int? = null,
    val monthlyPlanKopecks: Long? = null,
    val isActive: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "savings_ledger")
data class SavingsLedgerEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val amountKopecks: Long,
    val entryType: String,
    val transactionId: String? = null,
    val dateEpochDay: Long,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
