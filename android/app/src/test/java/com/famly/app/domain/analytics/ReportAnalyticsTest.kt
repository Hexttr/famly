package com.famly.app.domain.analytics

import com.famly.app.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReportAnalyticsTest {

    private val now = LocalDate.of(2025, 6, 15)

    private fun tx(
        id: String,
        amount: Long,
        type: String,
        date: LocalDate,
        categoryId: String = "cat1",
    ) = TransactionEntity(
        id = id,
        amountKopecks = amount,
        type = type,
        categoryId = categoryId,
        accountId = "acc1",
        dateEpochDay = date.toEpochDay(),
        note = null,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun filterTransactionsByPeriod_month_includesCurrentMonthOnly() {
        val txs = listOf(
            tx("1", 100, "expense", LocalDate.of(2025, 6, 10)),
            tx("2", 200, "expense", LocalDate.of(2025, 5, 20)),
        )
        val filtered = filterTransactionsByPeriod(txs, ReportPeriod.MONTH, now)
        assertEquals(1, filtered.size)
        assertEquals("1", filtered.first().id)
    }

    @Test
    fun filterTransactionsByPreviousPeriod_excludesCurrentMonth() {
        val txs = listOf(
            tx("1", 100, "expense", LocalDate.of(2025, 6, 10)),
            tx("2", 200, "expense", LocalDate.of(2025, 5, 20)),
        )
        val filtered = filterTransactionsByPreviousPeriod(txs, ReportPeriod.MONTH, now)
        assertEquals(1, filtered.size)
        assertEquals("2", filtered.first().id)
    }

    @Test
    fun getPeriodExpenseComparison_calculatesChangePercent() {
        val txs = listOf(
            tx("1", 1000, "expense", LocalDate.of(2025, 6, 10)),
            tx("2", 500, "expense", LocalDate.of(2025, 5, 10)),
        )
        val comparison = getPeriodExpenseComparison(txs, ReportPeriod.MONTH, now)
        assertEquals(1000L, comparison.currentExpensesKopecks)
        assertEquals(500L, comparison.previousExpensesKopecks)
        assertEquals(100, comparison.changePercent)
    }

    @Test
    fun getPeriodExpenseComparison_nullChangeWhenNoPrevious() {
        val txs = listOf(tx("1", 1000, "expense", LocalDate.of(2025, 6, 10)))
        val comparison = getPeriodExpenseComparison(txs, ReportPeriod.MONTH, now)
        assertNull(comparison.changePercent)
    }

    @Test
    fun getMonthlyTotals_returnsRequestedMonthCount() {
        val txs = listOf(
            tx("1", 300, "expense", LocalDate.of(2025, 6, 5)),
            tx("2", 700, "income", LocalDate.of(2025, 6, 8)),
            tx("3", 100, "expense", LocalDate.of(2025, 5, 12)),
        )
        val totals = getMonthlyTotals(txs, monthsCount = 3, now = now)
        assertEquals(3, totals.size)
        val june = totals.first { it.month == 6 }
        assertEquals(300L, june.expensesKopecks)
        assertEquals(700L, june.incomeKopecks)
    }

    @Test
    fun getTotalExpenses_sumsExpenseTypeOnly() {
        val txs = listOf(
            tx("1", 100, "expense", now),
            tx("2", 500, "income", now),
        )
        assertEquals(100L, getTotalExpenses(txs))
    }

    @Test
    fun getReportPeriodDescription_month_usesRussianLabel() {
        val desc = getReportPeriodDescription(ReportPeriod.MONTH, now)
        assertTrue(desc.contains("2025"))
    }
}
