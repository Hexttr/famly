package com.famly.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BudgetCalculatorTest {

    @Test
    fun currentPeriod_whenTodayAfterStartDay_startsThisMonth() {
        val today = LocalDate.of(2025, 6, 15)
        val period = BudgetCalculator.currentPeriod(startDay = 10, today = today)
        assertEquals(LocalDate.of(2025, 6, 10), period.start)
        assertEquals(LocalDate.of(2025, 7, 9), period.end)
    }

    @Test
    fun currentPeriod_whenTodayBeforeStartDay_startsPreviousMonth() {
        val today = LocalDate.of(2025, 6, 5)
        val period = BudgetCalculator.currentPeriod(startDay = 10, today = today)
        assertEquals(LocalDate.of(2025, 5, 10), period.start)
        assertEquals(LocalDate.of(2025, 6, 9), period.end)
    }

    @Test
    fun daysLeftInPeriod_countsRemainingDays() {
        val end = LocalDate.of(2025, 6, 10)
        val today = LocalDate.of(2025, 6, 3)
        assertEquals(7, BudgetCalculator.daysLeftInPeriod(end, today))
    }

    @Test
    fun safeToSpend_neverNegative() {
        assertEquals(0L, BudgetCalculator.safeToSpend(budgetTotalKopecks = 1000, spentKopecks = 5000))
        assertEquals(3000L, BudgetCalculator.safeToSpend(10000, 7000))
    }

    @Test
    fun dailySafeSpend_dividesRemainingByDays() {
        assertEquals(500L, BudgetCalculator.dailySafeSpend(3000, 6))
        assertEquals(0L, BudgetCalculator.dailySafeSpend(3000, 0))
    }
}
