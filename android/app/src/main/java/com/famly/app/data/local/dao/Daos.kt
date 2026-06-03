package com.famly.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
import com.famly.app.data.local.entity.SplitAllocationEntity
import com.famly.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<TransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY recurringDay, createdAt DESC")
    suspend fun getRecurringTemplates(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY recurringDay, createdAt DESC")
    fun observeRecurringTemplates(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY name")
    fun observeAll(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<FamilyMemberEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface IouBalanceDao {
    @Query("SELECT * FROM iou_balances WHERE settledAt IS NULL ORDER BY amountKopecks DESC")
    fun observeOpen(): Flow<List<IouBalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(balance: IouBalanceEntity)

    @Query("UPDATE iou_balances SET settledAt = :settledAt, updatedAt = :settledAt WHERE id = :id")
    suspend fun settle(id: String, settledAt: Long)
}

@Dao
interface SplitAllocationDao {
    @Query("SELECT * FROM split_allocations WHERE transactionId = :transactionId")
    suspend fun getByTransaction(transactionId: String): List<SplitAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(allocation: SplitAllocationEntity)

    @Query("DELETE FROM split_allocations WHERE transactionId = :transactionId")
    suspend fun deleteByTransaction(transactionId: String)
}
