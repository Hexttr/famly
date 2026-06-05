package com.famly.app.domain.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReportAnalyticsBaselineTest {

    private val now = LocalDate.of(2025, 6, 15)

    private fun expense(id: String, amount: Long, date: LocalDate, categoryId: String = "c1") =
        com.famly.app.data.local.entity.TransactionEntity(
            id = id,
            amountKopecks = amount,
            type = "expense",
            categoryId = categoryId,
            accountId = "a1",
            dateEpochDay = date.toEpochDay(),
            note = null,
            createdAt = 0,
            updatedAt = 0,
        )

    @Test
    fun categoryTrends_nullPercentWithoutPreviousPeriodData() {
        val categories = listOf(
            com.famly.app.data.local.entity.CategoryEntity(
                "c2", "Транспорт", "🚌", "expense", "#457B9D", 0L, 0, false, 0, 0, 0,
            ),
        )
        val txs = listOf(expense("1", 205_000, LocalDate.of(2025, 6, 10), "c2"))
        val trends = getCategoryExpenseTrends(categories, txs, ReportPeriod.MONTH, now)
        assertEquals(1, trends.size)
        assertNull(trends.first().changePercent)
    }

    @Test
    fun categoryTrends_calculatesWhenPreviousPeriodHasData() {
        val categories = listOf(
            com.famly.app.data.local.entity.CategoryEntity(
                "c2", "Транспорт", "🚌", "expense", "#457B9D", 0L, 0, false, 0, 0, 0,
            ),
        )
        val txs = listOf(
            expense("1", 200_000, LocalDate.of(2025, 6, 10), "c2"),
            expense("2", 100_000, LocalDate.of(2025, 5, 10), "c2"),
        )
        val trends = getCategoryExpenseTrends(categories, txs, ReportPeriod.MONTH, now)
        assertEquals(100, trends.first().changePercent)
    }
}
