package com.famly.app.domain.budget

import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.BudgetCalculator
import com.famly.app.domain.model.AppSettings
import java.time.LocalDate

object BudgetRolloverProcessor {
    data class RolloverUpdate(val categoryId: String, val rolloverKopecks: Long)

    fun process(
        settings: AppSettings,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        lastProcessedPeriodStart: Long?,
        today: LocalDate = LocalDate.now(),
    ): Pair<List<RolloverUpdate>, Long> {
        val period = BudgetCalculator.currentPeriod(settings.budgetPeriod.startDay, today)
        val periodStart = period.start.toEpochDay()
        if (lastProcessedPeriodStart == periodStart) {
            return emptyList<RolloverUpdate>() to periodStart
        }

        val previousPeriod = if (lastProcessedPeriodStart != null) {
            val prevStart = LocalDate.ofEpochDay(lastProcessedPeriodStart)
            val prevEnd = prevStart.plusMonths(1).minusDays(1)
            prevStart.toEpochDay()..prevEnd.toEpochDay()
        } else {
            val prevStart = period.start.minusMonths(1)
            val day = settings.budgetPeriod.startDay.coerceIn(1, 28)
            val adjusted = prevStart.withDayOfMonth(minOf(day, prevStart.lengthOfMonth()))
            val prevEnd = adjusted.plusMonths(1).minusDays(1)
            adjusted.toEpochDay()..prevEnd.toEpochDay()
        }

        val updates = categories
            .filter { it.type == "expense" && it.rolloverEnabled && (it.budgetLimitKopecks ?: 0L) > 0L }
            .mapNotNull { category ->
                val limit = category.budgetLimitKopecks ?: return@mapNotNull null
                val effectiveLimit = limit + category.rolloverKopecks
                val spent = transactions
                    .filter { it.categoryId == category.id && it.type == "expense" && it.dateEpochDay in previousPeriod }
                    .sumOf { it.amountKopecks }
                val unused = (effectiveLimit - spent).coerceAtLeast(0L)
                if (unused == category.rolloverKopecks) null
                else RolloverUpdate(category.id, unused)
            }

        return updates to periodStart
    }

    fun effectiveLimit(category: CategoryEntity): Long {
        val base = category.budgetLimitKopecks ?: 0L
        return base + category.rolloverKopecks
    }
}
