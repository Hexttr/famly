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
import com.famly.app.data.local.dao.SplitAllocationDao
import com.famly.app.data.local.dao.TransactionDao
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
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
    ],
    version = 2,
    exportSchema = false,
)
abstract class FamlyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun iouBalanceDao(): IouBalanceDao
    abstract fun splitAllocationDao(): SplitAllocationDao

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

        fun get(context: Context): FamlyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FamlyDatabase::class.java,
                    "famly.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
