package com.famly.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.repository.FamlyRepository
import com.famly.app.domain.BudgetCalculator
import com.famly.app.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val safeToSpendKopecks: Long = 0,
    val spentKopecks: Long = 0,
    val incomeKopecks: Long = 0,
    val budgetTotalKopecks: Long = 0,
    val daysLeft: Int = 0,
)

class FamlyViewModel(private val repository: FamlyRepository) : ViewModel() {
    val uiState: StateFlow<FamlyUiState> = combine(
        repository.settings,
        repository.accounts,
        repository.categories,
        repository.transactions,
    ) { settings, accounts, categories, transactions ->
        val period = BudgetCalculator.currentPeriod(settings.budgetPeriod.startDay)
        val startDay = period.start.toEpochDay()
        val endDay = period.end.toEpochDay()

        val periodTx = transactions.filter { it.dateEpochDay in startDay..endDay }
        val spent = periodTx.filter { it.type == "expense" }.sumOf { it.amountKopecks }
        val income = periodTx.filter { it.type == "income" }.sumOf { it.amountKopecks }
        val budgetTotal = categories.filter { it.type == "expense" }.sumOf { it.budgetLimitKopecks ?: 0L }

        FamlyUiState(
            settings = settings,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            safeToSpendKopecks = BudgetCalculator.safeToSpend(budgetTotal, spent),
            spentKopecks = spent,
            incomeKopecks = income,
            budgetTotalKopecks = budgetTotal,
            daysLeft = BudgetCalculator.daysLeftInPeriod(period.end),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamlyUiState())

    fun completeOnboarding() = viewModelScope.launch { repository.completeOnboarding() }
    fun setTheme(theme: String) = viewModelScope.launch { repository.setTheme(theme) }
    fun setBudgetStartDay(day: Int) = viewModelScope.launch { repository.setBudgetStartDay(day) }
    fun activatePremium() = viewModelScope.launch { repository.activatePremium() }

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
                icon = if (type == "expense") "📦" else "💵",
                type = type,
                color = if (type == "expense") "#457B9D" else "#2D6A4F",
                budgetLimitKopecks = if (type == "expense") 0L else null,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    fun deleteCategory(id: String) = viewModelScope.launch { repository.deleteCategory(id) }

    fun addAccount(name: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repository.upsertAccount(
            AccountEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = "💳",
                balanceKopecks = 0,
                color = "#2D6A4F",
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    fun deleteAccount(id: String) = viewModelScope.launch { repository.deleteAccount(id) }

    class Factory(private val repository: FamlyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FamlyViewModel(repository) as T
    }
}
