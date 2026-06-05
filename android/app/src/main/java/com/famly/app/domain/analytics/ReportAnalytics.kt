package com.famly.app.domain.analytics

import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.BudgetCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ReportPeriod { MONTH, THREE_MONTHS, SIX_MONTHS, YEAR }

data class MonthlyTotal(
    val key: String,
    val label: String,
    val year: Int,
    val month: Int,
    val expensesKopecks: Long,
    val incomeKopecks: Long,
)

data class PeriodComparison(
    val currentExpensesKopecks: Long,
    val previousExpensesKopecks: Long,
    val changePercent: Int?,
)

data class CategoryTrend(
    val categoryId: String,
    val name: String,
    val icon: String,
    val color: String,
    val currentKopecks: Long,
    val previousKopecks: Long,
    val changePercent: Int?,
)

data class BudgetWarning(
    val categoryId: String,
    val spentKopecks: Long,
    val limitKopecks: Long,
    val percent: Float,
)

val REPORT_PERIOD_LABELS = mapOf(
    ReportPeriod.MONTH to "Месяц",
    ReportPeriod.THREE_MONTHS to "3 мес.",
    ReportPeriod.SIX_MONTHS to "Полгода",
    ReportPeriod.YEAR to "Год",
)

private val MONTHS_SHORT = listOf(
    "Янв", "Фев", "Мар", "Апр", "Май", "Июн",
    "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек",
)

fun getMonthsCountForPeriod(period: ReportPeriod): Int = when (period) {
    ReportPeriod.MONTH -> 1
    ReportPeriod.THREE_MONTHS -> 3
    ReportPeriod.SIX_MONTHS -> 6
    ReportPeriod.YEAR -> 12
}

fun getReportPeriodStart(period: ReportPeriod, now: LocalDate = LocalDate.now()): LocalDate =
    when (period) {
        ReportPeriod.MONTH -> now.withDayOfMonth(1)
        ReportPeriod.THREE_MONTHS -> now.minusMonths(2).withDayOfMonth(1)
        ReportPeriod.SIX_MONTHS -> now.minusMonths(5).withDayOfMonth(1)
        ReportPeriod.YEAR -> now.minusMonths(11).withDayOfMonth(1)
    }

fun filterTransactionsByPeriod(
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): List<TransactionEntity> {
    val start = getReportPeriodStart(period, now).toEpochDay()
    val end = now.toEpochDay()
    return transactions.filter { it.dateEpochDay in start..end }
}

fun filterTransactionsByPreviousPeriod(
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): List<TransactionEntity> {
    val currentStart = getReportPeriodStart(period, now)
    val prevEnd = currentStart.minusDays(1)
    val prevStart = when (period) {
        ReportPeriod.MONTH -> currentStart.minusMonths(1)
        ReportPeriod.THREE_MONTHS -> currentStart.minusMonths(3)
        ReportPeriod.SIX_MONTHS -> currentStart.minusMonths(6)
        ReportPeriod.YEAR -> currentStart.minusYears(1)
    }
    return transactions.filter { it.dateEpochDay in prevStart.toEpochDay()..prevEnd.toEpochDay() }
}

fun getReportPeriodDescription(period: ReportPeriod, now: LocalDate = LocalDate.now()): String {
    if (period == ReportPeriod.MONTH) {
        return com.famly.app.domain.MoneyFormatter.formatPeriodLabel(now)
    }
    val start = getReportPeriodStart(period, now)
    val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return "${start.format(fmt)} — ${now.format(fmt)}"
}

fun getTotalExpenses(transactions: List<TransactionEntity>): Long =
    transactions.filter { it.type == "expense" }.sumOf { it.amountKopecks }

fun getTotalIncome(transactions: List<TransactionEntity>): Long =
    transactions.filter { it.type == "income" }.sumOf { it.amountKopecks }

fun getCategorySpent(categoryId: String, transactions: List<TransactionEntity>): Long =
    transactions.filter { it.categoryId == categoryId && it.type == "expense" }
        .sumOf { it.amountKopecks }

fun hasPeriodComparisonBaseline(
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): Boolean = filterTransactionsByPreviousPeriod(transactions, period, now)
    .any { it.type == "expense" }

fun getPeriodExpenseComparison(
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): PeriodComparison {
    val current = getTotalExpenses(filterTransactionsByPeriod(transactions, period, now))
    val previous = getTotalExpenses(filterTransactionsByPreviousPeriod(transactions, period, now))
    val changePercent = if (previous > 0) {
        ((current - previous) * 100 / previous).toInt()
    } else null
    return PeriodComparison(current, previous, changePercent)
}

fun getMonthlyTotals(
    transactions: List<TransactionEntity>,
    monthsCount: Int,
    now: LocalDate = LocalDate.now(),
): List<MonthlyTotal> {
    return (monthsCount - 1 downTo 0).map { offset ->
        val d = now.minusMonths(offset.toLong())
        val year = d.year
        val month = d.monthValue
        val monthTx = transactions.filter {
            val td = LocalDate.ofEpochDay(it.dateEpochDay)
            td.year == year && td.monthValue == month
        }
        MonthlyTotal(
            key = "$year-${month.toString().padStart(2, '0')}",
            label = MONTHS_SHORT[month - 1],
            year = year,
            month = month,
            expensesKopecks = getTotalExpenses(monthTx),
            incomeKopecks = getTotalIncome(monthTx),
        )
    }
}

fun getCategoryExpenseTrends(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): List<CategoryTrend> {
    val hasBaseline = hasPeriodComparisonBaseline(transactions, period, now)
    val currentTx = filterTransactionsByPeriod(transactions, period, now)
    val previousTx = filterTransactionsByPreviousPeriod(transactions, period, now)
    return categories.filter { it.type == "expense" }.map { cat ->
        val current = getCategorySpent(cat.id, currentTx)
        val previous = getCategorySpent(cat.id, previousTx)
        val changePercent = when {
            !hasBaseline -> null
            previous > 0 -> ((current - previous) * 100 / previous).toInt()
            current > 0 -> 100
            else -> null
        }
        CategoryTrend(cat.id, cat.name, cat.icon, cat.color, current, previous, changePercent)
    }.filter { it.currentKopecks > 0 || it.previousKopecks > 0 }
        .sortedByDescending { kotlin.math.abs(it.changePercent ?: 0) }
}

fun getBudgetWarnings(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    threshold: Float = 0.8f,
): List<BudgetWarning> =
    categories.filter { it.type == "expense" && it.budgetLimitKopecks != null && it.budgetLimitKopecks > 0 }
        .map { cat ->
            val spent = getCategorySpent(cat.id, transactions)
            val limit = cat.budgetLimitKopecks ?: 0L
            BudgetWarning(cat.id, spent, limit, if (limit > 0) spent.toFloat() / limit else 0f)
        }
        .filter { it.percent >= threshold }
        .sortedByDescending { it.percent }

fun getAverageDailyExpense(
    transactions: List<TransactionEntity>,
    period: ReportPeriod,
    now: LocalDate = LocalDate.now(),
): Long {
    val total = getTotalExpenses(filterTransactionsByPeriod(transactions, period, now))
    val start = getReportPeriodStart(period, now)
    val days = maxOf(1, java.time.temporal.ChronoUnit.DAYS.between(start, now).toInt() + 1)
    return total / days
}

fun getDailySafeSpend(remainingKopecks: Long, daysLeft: Int): Long =
    BudgetCalculator.dailySafeSpend(remainingKopecks, daysLeft)

fun getTopExpenseCategoryIds(transactions: List<TransactionEntity>, limit: Int = 4): List<String> {
    val counts = mutableMapOf<String, Int>()
    transactions.forEach { tx ->
        if (tx.type == "expense") {
            counts[tx.categoryId] = (counts[tx.categoryId] ?: 0) + 1
        }
    }
    return counts.entries
        .sortedByDescending { it.value }
        .take(limit)
        .map { it.key }
}

fun getBudgetUsedPercent(spentKopecks: Long, limitKopecks: Long): Int =
    if (limitKopecks <= 0) 0 else minOf(100, (spentKopecks * 100 / limitKopecks).toInt())
