package com.famly.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.famly.app.data.local.dao.AccountDao
import com.famly.app.data.local.dao.CategoryDao
import com.famly.app.data.local.dao.FamilyMemberDao
import com.famly.app.data.local.dao.IouBalanceDao
import com.famly.app.data.local.dao.PendingSyncDao
import com.famly.app.data.local.dao.SavingsGoalDao
import com.famly.app.data.local.dao.SavingsLedgerDao
import com.famly.app.data.local.dao.SplitAllocationDao
import com.famly.app.data.local.dao.TransactionDao
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
import com.famly.app.data.local.entity.PendingSyncEntity
import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.SplitAllocationEntity
import com.famly.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        FamilyMemberEntity::class,
        IouBalanceEntity::class,
        SplitAllocationEntity::class,
        PendingSyncEntity::class,
        SavingsGoalEntity::class,
        SavingsLedgerEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class FamlyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun iouBalanceDao(): IouBalanceDao
    abstract fun splitAllocationDao(): SplitAllocationDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun savingsLedgerDao(): SavingsLedgerDao

    companion object {
        @Volatile
        private var instance: FamlyDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN recurringDay INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN splitMemberIds TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN rolloverKopecks INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS family_members (
                        id TEXT NOT NULL PRIMARY KEY,
                        householdId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        visibility TEXT NOT NULL,
                        avatar TEXT NOT NULL,
                        syncVersion INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS iou_balances (
                        id TEXT NOT NULL PRIMARY KEY,
                        fromMemberId TEXT NOT NULL,
                        toMemberId TEXT NOT NULL,
                        amountKopecks INTEGER NOT NULL,
                        settledAt INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS split_allocations (
                        id TEXT NOT NULL PRIMARY KEY,
                        transactionId TEXT NOT NULL,
                        memberId TEXT NOT NULL,
                        shareKopecks INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN lastRecurrenceEpochDay INTEGER")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN rolloverEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE family_members ADD COLUMN userId TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pending_sync (
                        compositeKey TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        payload TEXT NOT NULL,
                        syncVersion INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        deleted INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id TEXT NOT NULL PRIMARY KEY,
                        householdId TEXT NOT NULL,
                        goalType TEXT NOT NULL,
                        customName TEXT,
                        targetKopecks INTEGER NOT NULL,
                        savedKopecks INTEGER NOT NULL DEFAULT 0,
                        incomePercent INTEGER,
                        monthlyPlanKopecks INTEGER,
                        isActive INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS savings_ledger (
                        id TEXT NOT NULL PRIMARY KEY,
                        goalId TEXT NOT NULL,
                        amountKopecks INTEGER NOT NULL,
                        entryType TEXT NOT NULL,
                        transactionId TEXT,
                        dateEpochDay INTEGER NOT NULL,
                        note TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        fun get(context: Context): FamlyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FamlyDatabase::class.java,
                    "famly.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { instance = it }
            }
    }
}
