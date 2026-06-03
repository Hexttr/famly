package com.famly.app.domain.recurring

import com.famly.app.data.local.entity.TransactionEntity
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

object RecurringProcessor {

    fun effectiveRecurringDay(recurringDay: Int?, fallbackEpochDay: Long): Int {
        val day = recurringDay ?: LocalDate.ofEpochDay(fallbackEpochDay).dayOfMonth
        return day.coerceIn(1, 28)
    }

    fun matchesToday(template: TransactionEntity, today: LocalDate): Boolean {
        val recurringDay = effectiveRecurringDay(template.recurringDay, template.dateEpochDay)
        return when {
            today.dayOfMonth == recurringDay -> true
            today.dayOfMonth > 28 && recurringDay == 28 -> true
            else -> false
        }
    }

    fun isDue(template: TransactionEntity, today: LocalDate = LocalDate.now()): Boolean {
        if (!template.isRecurring) return false
        if (!matchesToday(template, today)) return false

        val currentMonth = YearMonth.from(today)
        val lastRecurrence = template.lastRecurrenceEpochDay?.let(LocalDate::ofEpochDay)
        if (lastRecurrence == null) {
            val originalMonth = YearMonth.from(LocalDate.ofEpochDay(template.dateEpochDay))
            return originalMonth < currentMonth
        }
        return YearMonth.from(lastRecurrence) < currentMonth
    }

    fun nextDueLabel(template: TransactionEntity, today: LocalDate = LocalDate.now()): String {
        val day = effectiveRecurringDay(template.recurringDay, template.dateEpochDay)
        val targetMonth = if (isDue(template, today) || matchesToday(template, today)) {
            YearMonth.from(today)
        } else if (today.dayOfMonth < day.coerceAtMost(28)) {
            YearMonth.from(today)
        } else {
            YearMonth.from(today).plusMonths(1)
        }
        val safeDay = minOf(day, targetMonth.lengthOfMonth()).coerceAtMost(28)
        return "${targetMonth.monthValue.toString().padStart(2, '0')}.${targetMonth.year} · $safeDay число"
    }

    fun createCopy(
        template: TransactionEntity,
        today: LocalDate = LocalDate.now(),
        now: Long = System.currentTimeMillis(),
    ): TransactionEntity =
        TransactionEntity(
            id = UUID.randomUUID().toString(),
            amountKopecks = template.amountKopecks,
            type = template.type,
            categoryId = template.categoryId,
            accountId = template.accountId,
            dateEpochDay = today.toEpochDay(),
            note = template.note?.let { "$it (авто)" } ?: "Периодический платёж",
            isRecurring = false,
            recurringDay = null,
            lastRecurrenceEpochDay = null,
            isPrivate = template.isPrivate,
            splitMemberIds = null,
            createdAt = now,
            updatedAt = now,
        )

    fun withRecurrenceRecorded(template: TransactionEntity, today: LocalDate): TransactionEntity =
        template.copy(
            lastRecurrenceEpochDay = today.toEpochDay(),
            updatedAt = System.currentTimeMillis(),
        )
}
