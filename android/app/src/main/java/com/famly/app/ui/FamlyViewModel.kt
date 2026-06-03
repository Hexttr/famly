package com.famly.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.famly.app.billing.FamlyBillingManager
import com.famly.app.billing.PremiumPlan
import com.famly.app.billing.PurchaseResult
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.repository.FamlyRepository
import com.famly.app.data.sync.SyncRepository
import com.famly.app.data.sync.SyncStatus
import com.famly.app.domain.BudgetCalculator
import com.famly.app.domain.DEFAULT_ACCOUNT_ICON
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.domain.analytics.getDailySafeSpend
import com.famly.app.domain.iou.IouBalance
import com.famly.app.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class FamlyUiState(
    val settings: AppSettings = AppSettings(
        theme = "light",
        budgetPeriod = com.famly.app.domain.model.BudgetPeriod(28, "monthly"),
        currency = "RUB",
        onboardingComplete = false,
        isPremium = false,
        trialEndsAt = null,
        premiumExpiresAt = null,
    ),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val familyMembers: List<FamilyMemberEntity> = emptyList(),
    val iouBalances: List<IouBalance> = emptyList(),
    val safeToSpendKopecks: Long = 0,
    val spentKopecks: Long = 0,
    val incomeKopecks: Long = 0,
    val budgetTotalKopecks: Long = 0,
    val daysLeft: Int = 0,
    val dailySafeSpendKopecks: Long = 0,
    val periodLabel: String = "",
)

class FamlyViewModel(
    private val repository: FamlyRepository,
    private val syncRepository: SyncRepository,
    private val billingManager: FamlyBillingManager,
) : ViewModel() {
    private val _syncStatus = MutableStateFlow<SyncStatus?>(null)
    val syncStatus: StateFlow<SyncStatus?> = _syncStatus.asStateFlow()

    val uiState: StateFlow<FamlyUiState> = combine(
        combine(
            repository.settings,
            repository.accounts,
            repository.categories,
            repository.transactions,
        ) { settings, accounts, categories, transactions ->
            arrayOf(settings, accounts, categories, transactions)
        },
        combine(repository.familyMembers, repository.nettedIouBalances) { family, iou ->
            Pair(family, iou)
        },
    ) { core, familyIou ->
        @Suppress("UNCHECKED_CAST")
        val settings = core[0] as AppSettings
        @Suppress("UNCHECKED_CAST")
        val accounts = core[1] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = core[2] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val transactions = core[3] as List<TransactionEntity>
        val family = familyIou.first
        val iou = familyIou.second
        val period = BudgetCalculator.currentPeriod(settings.budgetPeriod.startDay)
        val startDay = period.start.toEpochDay()
        val endDay = period.end.toEpochDay()

        val periodTx = transactions.filter { it.dateEpochDay in startDay..endDay }
        val spent = periodTx.filter { it.type == "expense" }.sumOf { it.amountKopecks }
        val income = periodTx.filter { it.type == "income" }.sumOf { it.amountKopecks }
        val budgetTotal = categories.filter { it.type == "expense" }.sumOf { it.budgetLimitKopecks ?: 0L }
        val remaining = BudgetCalculator.safeToSpend(budgetTotal, spent)
        val daysLeft = BudgetCalculator.daysLeftInPeriod(period.end)

        FamlyUiState(
            settings = settings,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            familyMembers = family,
            iouBalances = iou,
            safeToSpendKopecks = remaining,
            spentKopecks = spent,
            incomeKopecks = income,
            budgetTotalKopecks = budgetTotal,
            daysLeft = daysLeft,
            dailySafeSpendKopecks = getDailySafeSpend(remaining, daysLeft),
            periodLabel = MoneyFormatter.formatPeriodLabel(period.start),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamlyUiState())

    fun completeOnboarding() = viewModelScope.launch { repository.completeOnboarding() }
    fun setTheme(theme: String) = viewModelScope.launch { repository.setTheme(theme) }
    fun setBudgetStartDay(day: Int) = viewModelScope.launch { repository.setBudgetStartDay(day) }
    fun setCurrency(currency: String) = viewModelScope.launch { repository.setCurrency(currency) }
    fun activatePremium() = viewModelScope.launch { repository.activatePremium() }

    fun purchasePremium(plan: PremiumPlan = PremiumPlan.MONTHLY) = viewModelScope.launch {
        val result = when (plan) {
            PremiumPlan.MONTHLY -> billingManager.purchaseMonthly()
            PremiumPlan.YEARLY -> billingManager.purchaseYearly()
        }
        if (result is PurchaseResult.Success) {
            repository.activatePremium()
        }
    }

    fun syncNow() = viewModelScope.launch {
        _syncStatus.value = syncRepository.sync()
    }

    fun register(email: String, password: String, displayName: String) = viewModelScope.launch {
        _syncStatus.value = syncRepository.register(email, password, displayName)
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _syncStatus.value = syncRepository.login(email, password)
    }

    fun createHousehold(name: String) = viewModelScope.launch {
        _syncStatus.value = syncRepository.createHousehold(name)
    }

    fun joinHousehold(inviteCode: String) = viewModelScope.launch {
        _syncStatus.value = syncRepository.joinHousehold(inviteCode)
    }

    fun addTransaction(
        amountRubles: String,
        type: String,
        categoryId: String,
        accountId: String,
        note: String?,
        isRecurring: Boolean,
    ) = viewModelScope.launch {
        val amount = (amountRubles.replace(',', '.').toDoubleOrNull() ?: return@launch) * 100
        repository.addTransaction(
            amountKopecks = amount.toLong(),
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            dateEpochDay = LocalDate.now().toEpochDay(),
            note = note?.takeIf { it.isNotBlank() },
            isRecurring = isRecurring,
        )
    }

    fun deleteTransaction(id: String) = viewModelScope.launch { repository.deleteTransaction(id) }

    fun saveSplit(transactionId: String, memberIds: List<String>) =
        viewModelScope.launch { repository.saveSplit(transactionId, memberIds) }

    fun settleIou(iouId: String) = viewModelScope.launch { repository.settleIou(iouId) }

    fun settleIouBetween(fromMemberId: String, toMemberId: String) =
        viewModelScope.launch { repository.settleIouBetween(fromMemberId, toMemberId) }

    fun updateFamilyMember(
        memberId: String,
        role: String? = null,
        visibility: String? = null,
    ) = viewModelScope.launch {
        val member = uiState.value.familyMembers.find { it.id == memberId } ?: return@launch
        repository.updateFamilyMember(
            member.copy(
                role = role ?: member.role,
                visibility = visibility ?: member.visibility,
            ),
        )
    }

    fun cycleAccountIcon(accountId: String) =
        viewModelScope.launch { repository.cycleAccountIcon(accountId) }

    fun cycleCategoryIcon(categoryId: String) =
        viewModelScope.launch { repository.cycleCategoryIcon(categoryId) }

    fun updateCategoryBudget(categoryId: String, limitRubles: Long) = viewModelScope.launch {
        val cat = uiState.value.categories.find { it.id == categoryId } ?: return@launch
        repository.upsertCategory(cat.copy(budgetLimitKopecks = limitRubles * 100))
    }

    fun addCategory(name: String, type: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repository.upsertCategory(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = if (type == "expense") "📦" else "💰",
                type = type,
                color = if (type == "expense") "#457B9D" else "#2D6A4F",
                budgetLimitKopecks = if (type == "expense") 0L else null,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    fun deleteCategory(id: String) = viewModelScope.launch { repository.deleteCategory(id) }

    fun addAccount(name: String, icon: String = DEFAULT_ACCOUNT_ICON) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repository.upsertAccount(
            AccountEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                balanceKopecks = 0,
                color = "#2D6A4F",
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    fun deleteAccount(id: String) = viewModelScope.launch { repository.deleteAccount(id) }

    suspend fun exportBackupJson(): String = repository.exportBackupJson()

    suspend fun exportCsv(period: ReportPeriod = ReportPeriod.MONTH): String =
        repository.exportCsv(period)

    suspend fun exportExcel(period: ReportPeriod = ReportPeriod.MONTH): ByteArray =
        repository.exportExcel(period)

    class Factory(
        private val repository: FamlyRepository,
        private val syncRepository: SyncRepository,
        private val billingManager: FamlyBillingManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FamlyViewModel(repository, syncRepository, billingManager) as T
    }
}
