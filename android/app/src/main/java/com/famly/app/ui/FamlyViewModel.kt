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
import com.famly.app.domain.budget.BudgetRolloverProcessor
import com.famly.app.domain.DEFAULT_ACCOUNT_ICON
import com.famly.app.domain.DEFAULT_EXPENSE_ICON
import com.famly.app.domain.DEFAULT_INCOME_ICON
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

    private val _inviteCode = MutableStateFlow<String?>(null)
    val inviteCode: StateFlow<String?> = _inviteCode.asStateFlow()

    private val _inviteError = MutableStateFlow<String?>(null)
    val inviteError: StateFlow<String?> = _inviteError.asStateFlow()

    private val _inviteLoading = MutableStateFlow(false)
    val inviteLoading: StateFlow<Boolean> = _inviteLoading.asStateFlow()

    fun dismissNotification(id: String) = viewModelScope.launch {
        repository.dismissNotification(id)
    }

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
        val budgetTotal = categories.filter { it.type == "expense" }.sumOf { BudgetRolloverProcessor.effectiveLimit(it) }
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

    private fun autoSyncAfterAuth() = viewModelScope.launch {
        _syncStatus.value = syncRepository.sync()
    }

    fun register(email: String, password: String, displayName: String) = viewModelScope.launch {
        val status = syncRepository.register(email, password, displayName)
        _syncStatus.value = status
        if (status.success) autoSyncAfterAuth()
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        val status = syncRepository.login(email, password)
        _syncStatus.value = status
        if (status.success) autoSyncAfterAuth()
    }

    fun createHousehold(name: String) = viewModelScope.launch {
        val status = syncRepository.createHousehold(name)
        _syncStatus.value = status
        if (status.success) autoSyncAfterAuth()
    }

    fun joinHousehold(inviteCode: String) = viewModelScope.launch {
        val status = syncRepository.joinHousehold(inviteCode)
        _syncStatus.value = status
        if (status.success) autoSyncAfterAuth()
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

    fun updateTransactionRecurring(transactionId: String, isRecurring: Boolean, recurringDay: Int?) =
        viewModelScope.launch {
            repository.updateTransactionRecurring(transactionId, isRecurring, recurringDay)
        }

    fun disableRecurring(transactionId: String) =
        viewModelScope.launch { repository.disableRecurring(transactionId) }

    fun generateInvite() = viewModelScope.launch {
        _inviteLoading.value = true
        _inviteError.value = null
        val settings = uiState.value.settings
        _inviteCode.value = runCatching {
            if (settings.isAuthenticated) {
                if (!settings.isSynced) {
                    val name = settings.householdName?.trim().orEmpty()
                    if (name.isNotEmpty()) {
                        val status = syncRepository.createHousehold(name)
                        if (!status.success) error(status.error ?: "Не удалось создать семью")
                    } else {
                        syncRepository.ensureHouseholdLinked()
                    }
                }
                syncRepository.generateInviteCode()
            } else {
                val name = settings.householdName?.trim().orEmpty()
                if (name.isEmpty()) error("Сначала укажите название семьи")
                repository.ensureLocalFamily(name)
                repository.getOrCreateLocalInviteCode()
            }
        }.onFailure { _inviteError.value = it.message ?: "Не удалось создать код" }.getOrNull()
        _inviteLoading.value = false
    }

    fun restoreLocalInvite() = viewModelScope.launch {
        if (uiState.value.settings.isAuthenticated) return@launch
        repository.getLocalInviteCode()?.let { _inviteCode.value = it }
    }

    fun setupFamily(name: String) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _inviteError.value = "Введите название семьи"
            return@launch
        }
        _inviteLoading.value = true
        _inviteError.value = null
        repository.ensureLocalFamily(trimmed)
        val settings = uiState.value.settings
        if (settings.isAuthenticated) {
            val status = if (!settings.isSynced) {
                syncRepository.createHousehold(trimmed)
            } else {
                SyncStatus(success = true)
            }
            if (!status.success) {
                _inviteError.value = status.error
                _inviteLoading.value = false
                return@launch
            }
            _inviteCode.value = runCatching { syncRepository.generateInviteCode() }
                .onFailure { _inviteError.value = it.message ?: "Не удалось создать код" }
                .getOrNull()
        } else {
            _inviteCode.value = repository.getOrCreateLocalInviteCode()
        }
        _inviteLoading.value = false
    }

    fun clearInvite() {
        _inviteCode.value = null
        _inviteError.value = null
    }

    fun inviteUrl(): String? = _inviteCode.value?.let { "famly://join?code=$it" }

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

    fun cycleMemberAvatar(memberId: String) =
        viewModelScope.launch { repository.cycleMemberAvatar(memberId) }

    fun cycleAccountIcon(accountId: String) =
        viewModelScope.launch { repository.cycleAccountIcon(accountId) }

    fun cycleCategoryIcon(categoryId: String) =
        viewModelScope.launch { repository.cycleCategoryIcon(categoryId) }

    fun updateCategoryBudget(categoryId: String, limitRubles: Long) = viewModelScope.launch {
        val cat = uiState.value.categories.find { it.id == categoryId } ?: return@launch
        repository.upsertCategory(cat.copy(budgetLimitKopecks = limitRubles * 100))
    }

    fun updateCategoryRollover(categoryId: String, enabled: Boolean) = viewModelScope.launch {
        repository.updateCategoryRollover(categoryId, enabled)
    }

    fun reorderBudgetCategories(orderedIds: List<String>) = viewModelScope.launch {
        repository.reorderCategories(orderedIds)
    }

    fun saveFamilyName(name: String) = viewModelScope.launch {
        repository.updateHouseholdName(name)
    }

    fun addCategory(
        name: String,
        type: String,
        icon: String? = null,
        color: String? = null,
    ) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repository.upsertCategory(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = icon ?: if (type == "expense") DEFAULT_EXPENSE_ICON else DEFAULT_INCOME_ICON,
                type = type,
                color = color ?: if (type == "expense") "#457B9D" else "#2D6A4F",
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
