package com.famly.app.domain.budget

import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.domain.model.AppSettings
import com.famly.app.domain.model.BudgetPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BudgetRolloverProcessorTest {

    @Test
    fun `rollover carries unused budget when enabled`() {
        val settings = AppSettings(
            theme = "light",
            budgetPeriod = BudgetPeriod(1, "monthly"),
            currency = "RUB",
            onboardingComplete = true,
            isPremium = false,
            trialEndsAt = null,
            premiumExpiresAt = null,
            lastRolloverPeriodStart = LocalDate.of(2026, 5, 1).toEpochDay(),
        )
        val category = CategoryEntity(
            id = "c1",
            name = "Food",
            icon = "🛒",
            type = "expense",
            color = "#E63946",
            budgetLimitKopecks = 10_000,
            rolloverKopecks = 0,
            rolloverEnabled = true,
            createdAt = 0,
            updatedAt = 0,
        )
        val transactions = listOf(
            com.famly.app.data.local.entity.TransactionEntity(
                id = "t1",
                amountKopecks = 4_000,
                type = "expense",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = LocalDate.of(2026, 5, 15).toEpochDay(),
                note = null,
                createdAt = 0,
                updatedAt = 0,
            ),
        )

        val (updates, periodStart) = BudgetRolloverProcessor.process(
            settings,
            listOf(category),
            transactions,
            settings.lastRolloverPeriodStart,
            today = LocalDate.of(2026, 6, 2),
        )

        assertEquals(LocalDate.of(2026, 6, 1).toEpochDay(), periodStart)
        assertEquals(1, updates.size)
        assertEquals(6_000L, updates.first().rolloverKopecks)
    }
}
