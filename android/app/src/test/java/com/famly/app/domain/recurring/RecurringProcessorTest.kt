package com.famly.app.domain.recurring

import com.famly.app.data.local.entity.TransactionEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecurringProcessorTest {

    private fun template(
        recurringDay: Int = 15,
        lastRecurrence: Long? = null,
        date: String = "2026-04-15",
    ) = TransactionEntity(
        id = "tpl1",
        amountKopecks = 50_000,
        type = "expense",
        categoryId = "c4",
        accountId = "a2",
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        note = "ЖКХ",
        isRecurring = true,
        recurringDay = recurringDay,
        lastRecurrenceEpochDay = lastRecurrence,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun isDue_whenSameMonthAsCreation_returnsFalse() {
        val tx = template(date = "2026-06-05", recurringDay = 5)
        assertFalse(RecurringProcessor.isDue(tx, LocalDate.parse("2026-06-05")))
    }

    @Test
    fun isDue_whenNextMonthAndMatchingDay_returnsTrue() {
        val tx = template(date = "2026-05-05", recurringDay = 5)
        assertTrue(RecurringProcessor.isDue(tx, LocalDate.parse("2026-06-05")))
    }

    @Test
    fun isDue_whenAlreadyProcessedThisMonth_returnsFalse() {
        val tx = template(
            recurringDay = 5,
            lastRecurrence = LocalDate.parse("2026-06-05").toEpochDay(),
        )
        assertFalse(RecurringProcessor.isDue(tx, LocalDate.parse("2026-06-05")))
    }

    @Test
    fun createCopy_isNotRecurring() {
        val copy = RecurringProcessor.createCopy(template(), LocalDate.parse("2026-06-05"))
        assertFalse(copy.isRecurring)
    }
}
