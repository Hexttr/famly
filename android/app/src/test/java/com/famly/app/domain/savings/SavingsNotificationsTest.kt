package com.famly.app.domain.savings

import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SavingsNotificationsTest {

    private val goal = SavingsGoalEntity(
        id = "household:hh",
        householdId = "hh",
        goalType = "car",
        customName = null,
        targetKopecks = 200_000_00,
        savedKopecks = 100_000_00,
        incomePercent = 10,
        monthlyPlanKopecks = 10_000_00,
        isActive = true,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun monthlyShortfall_whenBelow80PercentOfPlan() {
        val ledger = listOf(
            ledger("manual_add", 3_000_00, day = 5),
        )
        val notices = SavingsNotifications.buildNotifications(
            goal = goal,
            ledger = ledger,
            transactions = emptyList(),
            periodStartEpochDay = 1,
            periodEndEpochDay = 31,
            periodLabel = "Июнь 2026",
            dismissedIds = emptySet(),
            today = LocalDate.of(2026, 6, 20),
        )
        assertTrue(notices.any { it.id.startsWith("savings_monthly_short_") })
    }

    @Test
    fun noIncomeNotice_whenAutoPercentConfiguredAndNoIncomeMidPeriod() {
        val notices = SavingsNotifications.buildNotifications(
            goal = goal,
            ledger = emptyList(),
            transactions = emptyList(),
            periodStartEpochDay = LocalDate.of(2026, 6, 1).toEpochDay(),
            periodEndEpochDay = LocalDate.of(2026, 6, 30).toEpochDay(),
            periodLabel = "Июнь 2026",
            dismissedIds = emptySet(),
            today = LocalDate.of(2026, 6, 20),
        )
        assertTrue(notices.any { it.id.startsWith("savings_no_income_") })
    }

    @Test
    fun dismissedNotificationsAreFiltered() {
        val notices = SavingsNotifications.buildNotifications(
            goal = goal.copy(savedKopecks = goal.targetKopecks),
            ledger = emptyList(),
            transactions = emptyList(),
            periodStartEpochDay = 1,
            periodEndEpochDay = 31,
            periodLabel = "Июнь 2026",
            dismissedIds = setOf("savings_reached"),
            today = LocalDate.of(2026, 6, 20),
        )
        assertTrue(notices.none { it.id == "savings_reached" })
    }

    @Test
    fun milestone50Notification() {
        val notices = SavingsNotifications.buildNotifications(
            goal = goal,
            ledger = emptyList(),
            transactions = emptyList(),
            periodStartEpochDay = 1,
            periodEndEpochDay = 31,
            periodLabel = "Июнь 2026",
            dismissedIds = emptySet(),
        )
        assertTrue(notices.any { it.id == "savings_milestone_50" })
    }

    private fun ledger(type: String, amount: Long, day: Long) = SavingsLedgerEntity(
        id = "l-$day",
        goalId = goal.id,
        amountKopecks = amount,
        entryType = type,
        transactionId = null,
        dateEpochDay = day,
        note = null,
        createdAt = 0,
        updatedAt = 0,
    )
}
