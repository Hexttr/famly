package com.famly.app.data.repository

import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID

class FamlyRepository(
    private val db: FamlyDatabase,
    private val preferences: UserPreferences,
) {
    val settings: Flow<AppSettings> = preferences.settings
    val accounts: Flow<List<AccountEntity>> = db.accountDao().observeAll()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()

    fun observeTransaction(id: String): Flow<TransactionEntity?> =
        db.transactionDao().observeById(id)

    suspend fun ensureSeeded() {
        if (db.accountDao().observeAll().first().isNotEmpty()) return
        val now = System.currentTimeMillis()
        val today = LocalDate.now().toEpochDay()

        listOf(
            AccountEntity("a1", "Наличные", "💵", 1_250_000, "#52B788", 0, now, now),
            AccountEntity("a2", "Сбербанк", "💳", 8_730_000, "#2D6A4F", 1, now, now),
            AccountEntity("a3", "Накопления", "🏦", 15_000_000, "#40916C", 2, now, now),
        ).forEach { db.accountDao().upsert(it) }

        listOf(
            CategoryEntity("c1", "Продукты", "🛒", "expense", "#E63946", 2_500_000, 0, now, now),
            CategoryEntity("c2", "Транспорт", "🚌", "expense", "#457B9D", 800_000, 1, now, now),
            CategoryEntity("c3", "Кафе", "☕", "expense", "#F4A261", 600_000, 2, now, now),
            CategoryEntity("c4", "ЖКХ", "🏠", "expense", "#6D597A", 1_200_000, 3, now, now),
            CategoryEntity("c5", "Развлечения", "🎬", "expense", "#E76F51", 500_000, 4, now, now),
            CategoryEntity("c6", "Зарплата", "💰", "income", "#2D6A4F", null, 5, now, now),
            CategoryEntity("c7", "Фриланс", "💻", "income", "#40916C", null, 6, now, now),
        ).forEach { db.categoryDao().upsert(it) }

        listOf(
            TransactionEntity(UUID.randomUUID().toString(), 184_700, "expense", "c1", "a2", today, "Пятёрочка", false, now, now),
            TransactionEntity(UUID.randomUUID().toString(), 8_900, "expense", "c2", "a2", today, "Метро", false, now, now),
            TransactionEntity(UUID.randomUUID().toString(), 45_000, "expense", "c3", "a1", today - 1, "Кофейня", false, now, now),
            TransactionEntity(UUID.randomUUID().toString(), 8_500_000, "income", "c6", "a2", today - 5, "Зарплата", false, now, now),
            TransactionEntity(UUID.randomUUID().toString(), 320_000, "expense", "c4", "a2", today - 3, "Электричество", false, now, now),
        ).forEach { db.transactionDao().upsert(it) }

        preferences.initTrialIfNeeded()
    }

    suspend fun completeOnboarding() = preferences.setOnboardingComplete()
    suspend fun setTheme(theme: String) = preferences.setTheme(theme)
    suspend fun setBudgetStartDay(day: Int) = preferences.setBudgetStartDay(day)
    suspend fun activatePremium() = preferences.activatePremium()

    suspend fun addTransaction(
        amountKopecks: Long,
        type: String,
        categoryId: String,
        accountId: String,
        dateEpochDay: Long,
        note: String?,
        isRecurring: Boolean,
    ) {
        val now = System.currentTimeMillis()
        db.transactionDao().upsert(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                amountKopecks = amountKopecks,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                dateEpochDay = dateEpochDay,
                note = note,
                isRecurring = isRecurring,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun deleteTransaction(id: String) = db.transactionDao().delete(id)

    suspend fun upsertCategory(category: CategoryEntity) = db.categoryDao().upsert(category)
    suspend fun deleteCategory(id: String) = db.categoryDao().delete(id)

    suspend fun upsertAccount(account: AccountEntity) = db.accountDao().upsert(account)
    suspend fun deleteAccount(id: String) = db.accountDao().delete(id)

    suspend fun exportBackupJson(): String {
        val accounts = db.accountDao().observeAll().first()
        val categories = db.categoryDao().observeAll().first()
        val transactions = db.transactionDao().observeAll().first()
        return buildString {
            append("{\"accounts\":")
            append(accounts.size)
            append(",\"categories\":")
            append(categories.size)
            append(",\"transactions\":")
            append(transactions.size)
            append("}")
        }
    }
}
