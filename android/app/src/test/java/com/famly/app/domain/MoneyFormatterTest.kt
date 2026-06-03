package com.famly.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoneyFormatterTest {

    @Test
    fun formatKopecks_formatsWholeRubles() {
        assertEquals("1 500 ₽", MoneyFormatter.formatKopecks(150_000))
    }

    @Test
    fun formatKopecks_zeroAmount() {
        assertEquals("0 ₽", MoneyFormatter.formatKopecks(0))
    }

    @Test
    fun formatShortDate_fromEpochDay() {
        val epochDay = LocalDate.of(2025, 6, 3).toEpochDay()
        assertEquals("03-06-2025", MoneyFormatter.formatShortDate(epochDay))
    }

    @Test
    fun formatPeriodLabel_returnsRussianMonth() {
        val label = MoneyFormatter.formatPeriodLabel(LocalDate.of(2025, 3, 15))
        assertEquals("Март 2025", label)
    }

    @Test
    fun formatRubles_withCustomCurrency() {
        assertTrue(MoneyFormatter.formatRubles(42.0, "$").endsWith("$"))
    }
}
