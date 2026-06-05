package com.famly.app.domain.savings

import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.TransactionEntity
import java.time.LocalDate
import java.util.UUID

object SavingsGoalProcessor {

    fun incomeAllocationKopecks(amountKopecks: Long, incomePercent: Int): Long {
        if (incomePercent <= 0 || amountKopecks <= 0) return 0L
        return amountKopecks * incomePercent / 100
    }

    fun spendFromGoalAmount(requestedKopecks: Long, savedKopecks: Long): Long =
        minOf(requestedKopecks.coerceAtLeast(0), savedKopecks.coerceAtLeast(0))

    fun progressPercent(savedKopecks: Long, targetKopecks: Long): Int =
        if (targetKopecks <= 0) 0 else minOf(100, (savedKopecks * 100 / targetKopecks).toInt())

    fun monthlyContributionsKopecks(
        ledger: List<SavingsLedgerEntity>,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
    ): Long = ledger
        .filter { it.dateEpochDay in periodStartEpochDay..periodEndEpochDay }
        .filter { it.entryType == "income_auto" || it.entryType == "manual_add" }
        .sumOf { it.amountKopecks }
        .coerceAtLeast(0)

    fun createIncomeAutoEntry(
        goal: SavingsGoalEntity,
        transaction: TransactionEntity,
        amountKopecks: Long,
        now: Long = System.currentTimeMillis(),
    ): SavingsLedgerEntity? {
        if (amountKopecks <= 0) return null
        return SavingsLedgerEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountKopecks = amountKopecks,
            entryType = "income_auto",
            transactionId = transaction.id,
            dateEpochDay = transaction.dateEpochDay,
            note = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    fun createManualAddEntry(
        goal: SavingsGoalEntity,
        amountKopecks: Long,
        note: String? = null,
        dateEpochDay: Long = LocalDate.now().toEpochDay(),
        now: Long = System.currentTimeMillis(),
    ): SavingsLedgerEntity? {
        if (amountKopecks <= 0) return null
        return SavingsLedgerEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountKopecks = amountKopecks,
            entryType = "manual_add",
            transactionId = null,
            dateEpochDay = dateEpochDay,
            note = note,
            createdAt = now,
            updatedAt = now,
        )
    }

    fun createSpendFromGoalEntry(
        goal: SavingsGoalEntity,
        amountKopecks: Long,
        transactionId: String,
        dateEpochDay: Long,
        now: Long = System.currentTimeMillis(),
    ): SavingsLedgerEntity? {
        if (amountKopecks <= 0) return null
        return SavingsLedgerEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountKopecks = -amountKopecks,
            entryType = "spend_from_goal",
            transactionId = transactionId,
            dateEpochDay = dateEpochDay,
            note = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    fun createPauseReleaseEntry(
        goal: SavingsGoalEntity,
        amountKopecks: Long,
        dateEpochDay: Long = LocalDate.now().toEpochDay(),
        now: Long = System.currentTimeMillis(),
    ): SavingsLedgerEntity? {
        if (amountKopecks <= 0) return null
        return SavingsLedgerEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountKopecks = -amountKopecks,
            entryType = "pause_release",
            transactionId = null,
            dateEpochDay = dateEpochDay,
            note = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    const val MANUAL_ACCOUNT_NOTE_PREFIX = "accountId:"

    fun manualEntryAccountId(note: String?): String? =
        note?.takeIf { it.startsWith(MANUAL_ACCOUNT_NOTE_PREFIX) }
            ?.removePrefix(MANUAL_ACCOUNT_NOTE_PREFIX)
            ?.substringBefore('|')
            ?.takeIf { it.isNotBlank() }

    fun manualEntryUserNote(note: String?): String? =
        note?.substringAfter('|', "").takeIf { !it.isNullOrBlank() }

    fun manualEntryNote(accountId: String, userNote: String?): String =
        buildString {
            append(MANUAL_ACCOUNT_NOTE_PREFIX)
            append(accountId)
            if (!userNote.isNullOrBlank()) {
                append('|')
                append(userNote.trim())
            }
        }

    /** Distributes [savedKopecks] back to source accounts proportionally to positive ledger entries. */
    fun releaseAmountsByAccount(
        savedKopecks: Long,
        ledger: List<SavingsLedgerEntity>,
        resolveIncomeAutoAccount: (SavingsLedgerEntity) -> String?,
    ): Map<String, Long> {
        if (savedKopecks <= 0) return emptyMap()
        val sources = mutableMapOf<String, Long>()
        ledger.filter { it.amountKopecks > 0 && it.entryType in listOf("income_auto", "manual_add") }
            .forEach { entry ->
                val accountId = when (entry.entryType) {
                    "income_auto" -> resolveIncomeAutoAccount(entry)
                    "manual_add" -> manualEntryAccountId(entry.note)
                    else -> null
                } ?: return@forEach
                sources[accountId] = (sources[accountId] ?: 0L) + entry.amountKopecks
            }
        if (sources.isEmpty()) return emptyMap()
        val totalSources = sources.values.sum()
        if (totalSources <= 0) return emptyMap()
        val result = mutableMapOf<String, Long>()
        var assigned = 0L
        val entries = sources.entries.toList()
        entries.forEachIndexed { index, (accountId, weight) ->
            val amount = if (index == entries.lastIndex) {
                savedKopecks - assigned
            } else {
                (savedKopecks * weight / totalSources).also { assigned += it }
            }
            if (amount > 0) result[accountId] = amount
        }
        return result
    }

    fun milestoneThreshold(savedKopecks: Long, targetKopecks: Long): Int? {
        if (targetKopecks <= 0) return null
        val pct = progressPercent(savedKopecks, targetKopecks)
        return when {
            pct >= 100 -> 100
            pct >= 75 -> 75
            pct >= 50 -> 50
            pct >= 25 -> 25
            else -> null
        }
    }

    fun daysSinceLastContribution(ledger: List<SavingsLedgerEntity>, today: LocalDate = LocalDate.now()): Int? {
        val lastDay = ledger.maxOfOrNull { it.dateEpochDay } ?: return null
        val days = LocalDate.ofEpochDay(lastDay).until(today).days
        return days.coerceAtLeast(0)
    }
}
