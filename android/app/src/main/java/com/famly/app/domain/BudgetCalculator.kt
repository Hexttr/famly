package com.famly.app.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object BudgetCalculator {
    data class PeriodBounds(val start: LocalDate, val end: LocalDate)

    fun currentPeriod(startDay: Int, today: LocalDate = LocalDate.now()): PeriodBounds {
        val day = startDay.coerceIn(1, 28)
        val periodStart = if (today.dayOfMonth >= day) {
            today.withDayOfMonth(minOf(day, today.lengthOfMonth()))
        } else {
            today.minusMonths(1).let { prev ->
                prev.withDayOfMonth(minOf(day, prev.lengthOfMonth()))
            }
        }
        val periodEnd = periodStart.plusMonths(1).minusDays(1)
        return PeriodBounds(periodStart, periodEnd)
    }

    fun daysLeftInPeriod(end: LocalDate, today: LocalDate = LocalDate.now()): Int =
        maxOf(0, ChronoUnit.DAYS.between(today, end).toInt())

    fun safeToSpend(budgetTotalKopecks: Long, spentKopecks: Long): Long =
        maxOf(0L, budgetTotalKopecks - spentKopecks)
}
