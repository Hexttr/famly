package com.famly.app.domain.savings

import com.famly.app.data.local.entity.SavingsLedgerEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavingsGoalProcessorTest {

    @Test
    fun incomeAllocation_roundsDownInKopecks() {
        assertEquals(5000L, SavingsGoalProcessor.incomeAllocationKopecks(100_000, 5))
        assertEquals(3000L, SavingsGoalProcessor.incomeAllocationKopecks(100_000, 3))
        assertEquals(0L, SavingsGoalProcessor.incomeAllocationKopecks(100_000, 0))
    }

    @Test
    fun spendFromGoal_doesNotExceedSaved() {
        assertEquals(2000L, SavingsGoalProcessor.spendFromGoalAmount(5000, 2000))
        assertEquals(5000L, SavingsGoalProcessor.spendFromGoalAmount(5000, 8000))
        assertEquals(0L, SavingsGoalProcessor.spendFromGoalAmount(5000, 0))
    }

    @Test
    fun progressPercent_capsAt100() {
        assertEquals(50, SavingsGoalProcessor.progressPercent(50_000, 100_000))
        assertEquals(100, SavingsGoalProcessor.progressPercent(150_000, 100_000))
    }

    @Test
    fun monthlyContributions_sumsPositiveEntriesInPeriod() {
        val ledger = listOf(
            ledgerEntry("income_auto", 3000, "tx1", day = 10),
            ledgerEntry("manual_add", 2000, "manual", day = 12),
            ledgerEntry("spend_from_goal", -1000, "spend", day = 15),
            ledgerEntry("manual_add", 5000, "m2", day = 40),
        )
        assertEquals(5000L, SavingsGoalProcessor.monthlyContributionsKopecks(ledger, 1, 30))
    }

    @Test
    fun milestoneThreshold_returnsHighestReached() {
        assertEquals(25, SavingsGoalProcessor.milestoneThreshold(25_000, 100_000))
        assertEquals(50, SavingsGoalProcessor.milestoneThreshold(55_000, 100_000))
        assertEquals(100, SavingsGoalProcessor.milestoneThreshold(100_000, 100_000))
        assertNull(SavingsGoalProcessor.milestoneThreshold(10_000, 100_000))
    }

    @Test
    fun releaseAmountsByAccount_distributesProportionally() {
        val ledger = listOf(
            ledgerEntry("income_auto", 6000, "tx1"),
            ledgerEntry("income_auto", 4000, "tx2"),
        )
        val result = SavingsGoalProcessor.releaseAmountsByAccount(
            savedKopecks = 5000,
            ledger = ledger,
            resolveIncomeAutoAccount = { entry ->
                when (entry.transactionId) {
                    "tx1" -> "a1"
                    "tx2" -> "a2"
                    else -> null
                }
            },
        )
        assertEquals(3000L, result["a1"])
        assertEquals(2000L, result["a2"])
    }

    @Test
    fun manualEntryNote_roundTrip() {
        val note = SavingsGoalProcessor.manualEntryNote("acc-1", "на отпуск")
        assertEquals("acc-1", SavingsGoalProcessor.manualEntryAccountId(note))
        assertEquals("на отпуск", SavingsGoalProcessor.manualEntryUserNote(note))
    }

    private fun ledgerEntry(type: String, amount: Long, txId: String, day: Long = 10) = SavingsLedgerEntity(
        id = "e-$type-$txId",
        goalId = "household:hh",
        amountKopecks = amount,
        entryType = type,
        transactionId = txId,
        dateEpochDay = day,
        note = null,
        createdAt = 0,
        updatedAt = 0,
    )
}
