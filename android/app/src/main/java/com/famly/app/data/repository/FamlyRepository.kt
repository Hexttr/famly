package com.famly.app.data.repository

import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
import com.famly.app.data.local.entity.SplitAllocationEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.nextAccountIcon
import com.famly.app.domain.nextCategoryIcon
import com.famly.app.data.sync.SyncRepository
import com.famly.app.data.export.BackupExporter
import com.famly.app.domain.recurring.RecurringProcessor
import com.famly.app.data.export.CsvExporter
import com.famly.app.data.export.ExcelExporter
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.domain.analytics.filterTransactionsByPeriod
import com.famly.app.domain.analytics.getReportPeriodDescription
import com.famly.app.domain.iou.netIouBalances
import com.famly.app.domain.iou.IouBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.famly.app.domain.model.AppSettings
import java.time.LocalDate
import java.util.UUID

class FamlyRepository(
    private val db: FamlyDatabase,
    private val preferences: UserPreferences,
    private val syncRepository: SyncRepository? = null,
) {
    val settings: Flow<AppSettings> = preferences.settings
    val accounts: Flow<List<AccountEntity>> = db.accountDao().observeAll()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    val familyMembers: Flow<List<FamilyMemberEntity>> = db.familyMemberDao().observeAll()
    val iouBalances: Flow<List<IouBalanceEntity>> = db.iouBalanceDao().observeOpen()

    val recurringTransactions: Flow<List<TransactionEntity>> = db.transactionDao().observeRecurringTemplates()

    val nettedIouBalances: Flow<List<IouBalance>> = iouBalances.map { list ->
        netIouBalances(list.map { IouBalance(it.fromMemberId, it.toMemberId, it.amountKopecks) })
    }

    fun observeTransaction(id: String): Flow<TransactionEntity?> =
        db.transactionDao().observeById(id)

    fun observeFamilyMember(id: String): Flow<FamilyMemberEntity?> =
        db.familyMemberDao().observeById(id)

    suspend fun ensureSeeded() {
        if (db.accountDao().observeAll().first().isNotEmpty()) return
        val now = System.currentTimeMillis()

        FamlySeedData.accounts(now).forEach { db.accountDao().upsert(it) }
        FamlySeedData.categories(now).forEach { db.categoryDao().upsert(it) }
        FamlySeedData.transactions(now).forEach { db.transactionDao().upsert(it) }
        FamlySeedData.familyMembers(now).forEach { db.familyMemberDao().upsert(it) }
        FamlySeedData.iouBalances(now).forEach { db.iouBalanceDao().upsert(it) }

        preferences.initTrialIfNeeded()
    }

    suspend fun completeOnboarding() = preferences.setOnboardingComplete()
    suspend fun setTheme(theme: String) = preferences.setTheme(theme)
    suspend fun setBudgetStartDay(day: Int) = preferences.setBudgetStartDay(day)
    suspend fun setCurrency(currency: String) = preferences.setCurrency(currency)
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
        val recurringDay = if (isRecurring) RecurringProcessor.effectiveRecurringDay(null, dateEpochDay) else null
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            amountKopecks = amountKopecks,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            dateEpochDay = dateEpochDay,
            note = note,
            isRecurring = isRecurring,
            recurringDay = recurringDay,
            lastRecurrenceEpochDay = if (isRecurring) dateEpochDay else null,
            createdAt = now,
            updatedAt = now,
        )
        db.transactionDao().upsert(entity)
        applyBalanceDelta(accountId, type, amountKopecks)
        syncRepository?.enqueueTransaction(entity)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        val previous = db.transactionDao().observeById(transaction.id).first()
        db.transactionDao().upsert(transaction.copy(updatedAt = System.currentTimeMillis()))
        if (previous != null &&
            (previous.amountKopecks != transaction.amountKopecks ||
                previous.accountId != transaction.accountId ||
                previous.type != transaction.type)
        ) {
            applyBalanceDelta(previous.accountId, previous.type, -previous.amountKopecks)
            applyBalanceDelta(transaction.accountId, transaction.type, transaction.amountKopecks)
        }
        syncRepository?.enqueueTransaction(transaction)
    }

    suspend fun updateTransactionRecurring(
        transactionId: String,
        isRecurring: Boolean,
        recurringDay: Int?,
    ) {
        val tx = db.transactionDao().observeById(transactionId).first() ?: return
        val day = if (isRecurring) {
            (recurringDay ?: RecurringProcessor.effectiveRecurringDay(tx.recurringDay, tx.dateEpochDay))
                .coerceIn(1, 28)
        } else {
            null
        }
        val updated = tx.copy(
            isRecurring = isRecurring,
            recurringDay = day,
            lastRecurrenceEpochDay = when {
                !isRecurring -> null
                tx.lastRecurrenceEpochDay != null -> tx.lastRecurrenceEpochDay
                else -> tx.dateEpochDay
            },
            updatedAt = System.currentTimeMillis(),
        )
        db.transactionDao().upsert(updated)
        syncRepository?.enqueueTransaction(updated)
    }

    suspend fun disableRecurring(transactionId: String) {
        updateTransactionRecurring(transactionId, isRecurring = false, recurringDay = null)
    }

    suspend fun processDueRecurring(today: LocalDate = LocalDate.now()): Int {
        val templates = db.transactionDao().getRecurringTemplates()
        var created = 0
        templates.forEach { template ->
            if (!RecurringProcessor.isDue(template, today)) return@forEach
            val copy = RecurringProcessor.createCopy(template, today)
            db.transactionDao().upsert(copy)
            applyBalanceDelta(copy.accountId, copy.type, copy.amountKopecks)
            val updatedTemplate = RecurringProcessor.withRecurrenceRecorded(template, today)
            db.transactionDao().upsert(updatedTemplate)
            syncRepository?.enqueueTransaction(copy)
            syncRepository?.enqueueTransaction(updatedTemplate)
            created++
        }
        return created
    }

    suspend fun deleteTransaction(id: String) {
        val tx = db.transactionDao().observeById(id).first() ?: return
        db.transactionDao().delete(id)
        db.splitAllocationDao().deleteByTransaction(id)
        applyBalanceDelta(tx.accountId, tx.type, -tx.amountKopecks)
        syncRepository?.enqueueDeleted("transaction", id)
    }

    private suspend fun applyBalanceDelta(accountId: String, type: String, amountKopecks: Long) {
        val account = db.accountDao().getById(accountId) ?: return
        val delta = when (type) {
            "income" -> amountKopecks
            else -> -amountKopecks
        }
        db.accountDao().upsert(
            account.copy(
                balanceKopecks = account.balanceKopecks + delta,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun saveSplit(transactionId: String, memberIds: List<String>) {
        val tx = db.transactionDao().observeById(transactionId).first() ?: return
        val now = System.currentTimeMillis()
        db.splitAllocationDao().deleteByTransaction(transactionId)
        if (memberIds.isEmpty()) {
            db.transactionDao().upsert(tx.copy(splitMemberIds = null, updatedAt = now))
            return
        }
        val share = tx.amountKopecks / memberIds.size
        memberIds.forEach { memberId ->
            db.splitAllocationDao().upsert(
                SplitAllocationEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    memberId = memberId,
                    shareKopecks = share,
                    createdAt = now,
                ),
            )
        }
        db.transactionDao().upsert(
            tx.copy(
                splitMemberIds = memberIds.joinToString(","),
                updatedAt = now,
            ),
        )
        syncRepository?.enqueueTransaction(tx.copy(splitMemberIds = memberIds.joinToString(","), updatedAt = now))
    }

    suspend fun settleIou(iouId: String) {
        db.iouBalanceDao().settle(iouId, System.currentTimeMillis())
    }

    suspend fun settleIouBetween(fromMemberId: String, toMemberId: String) {
        val now = System.currentTimeMillis()
        db.iouBalanceDao().observeOpen().first()
            .filter {
                (it.fromMemberId == fromMemberId && it.toMemberId == toMemberId) ||
                    (it.fromMemberId == toMemberId && it.toMemberId == fromMemberId)
            }
            .forEach { db.iouBalanceDao().settle(it.id, now) }
    }

    suspend fun updateFamilyMember(member: FamilyMemberEntity) {
        val updated = member.copy(updatedAt = System.currentTimeMillis())
        db.familyMemberDao().upsert(updated)
        syncRepository?.enqueueFamilyMember(updated)
    }

    suspend fun cycleAccountIcon(accountId: String) {
        val account = db.accountDao().getById(accountId) ?: return
        db.accountDao().upsert(
            account.copy(
                icon = nextAccountIcon(account.icon),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun cycleCategoryIcon(categoryId: String) {
        val cat = db.categoryDao().getById(categoryId) ?: return
        db.categoryDao().upsert(
            cat.copy(
                icon = nextCategoryIcon(cat.icon, cat.type),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun upsertCategory(category: CategoryEntity) {
        db.categoryDao().upsert(category)
        syncRepository?.enqueueCategory(category)
    }
    suspend fun deleteCategory(id: String) {
        db.categoryDao().delete(id)
        syncRepository?.enqueueDeleted("category", id)
    }

    suspend fun upsertAccount(account: AccountEntity) {
        db.accountDao().upsert(account)
        syncRepository?.enqueueAccount(account)
    }
    suspend fun deleteAccount(id: String) {
        db.accountDao().delete(id)
        syncRepository?.enqueueDeleted("account", id)
    }

    suspend fun getSplitAllocations(transactionId: String): List<SplitAllocationEntity> =
        db.splitAllocationDao().getByTransaction(transactionId)

    suspend fun exportBackupJson(): String {
        val settings = preferences.settings.first()
        return BackupExporter.export(
            accounts = db.accountDao().observeAll().first(),
            categories = db.categoryDao().observeAll().first(),
            transactions = db.transactionDao().observeAll().first(),
            familyMembers = db.familyMemberDao().observeAll().first(),
            iouBalances = db.iouBalanceDao().observeOpen().first(),
            settings = settings,
        )
    }

    suspend fun exportCsv(period: ReportPeriod = ReportPeriod.MONTH, daysLimit: Int? = null): String {
        val settings = preferences.settings.first()
        val transactions = filterTransactionsForExport(
            db.transactionDao().observeAll().first(),
            period,
            settings,
            daysLimit,
        )
        return CsvExporter.export(
            transactions = transactions,
            categories = db.categoryDao().observeAll().first(),
            accounts = db.accountDao().observeAll().first(),
        )
    }

    suspend fun exportExcel(period: ReportPeriod = ReportPeriod.MONTH, daysLimit: Int? = null): ByteArray {
        val settings = preferences.settings.first()
        val transactions = filterTransactionsForExport(
            db.transactionDao().observeAll().first(),
            period,
            settings,
            daysLimit,
        )
        return ExcelExporter.export(
            transactions = transactions,
            categories = db.categoryDao().observeAll().first(),
            periodDescription = getReportPeriodDescription(period),
        )
    }

    private fun filterTransactionsForExport(
        transactions: List<TransactionEntity>,
        period: ReportPeriod,
        settings: AppSettings,
        requestedDaysLimit: Int?,
    ): List<TransactionEntity> {
        if (settings.hasPremiumAccess()) {
            return filterTransactionsByPeriod(transactions, period)
        }
        val daysLimit = resolveExportDaysLimit(settings, requestedDaysLimit) ?: FREE_TIER_EXPORT_DAYS
        val cutoff = LocalDate.now().minusDays(daysLimit.toLong()).toEpochDay()
        return transactions.filter { it.dateEpochDay >= cutoff }
    }

    /** Free tier exports are limited to the last 30 days. */
    private fun resolveExportDaysLimit(settings: AppSettings, requested: Int?): Int? {
        if (settings.hasPremiumAccess()) return requested
        return requested?.coerceAtMost(FREE_TIER_EXPORT_DAYS) ?: FREE_TIER_EXPORT_DAYS
    }

    companion object {
        private const val FREE_TIER_EXPORT_DAYS = 30
    }
}
