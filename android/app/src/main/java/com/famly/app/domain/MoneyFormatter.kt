package com.famly.app.domain

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object MoneyFormatter {
    private val rubFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale("ru", "RU")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

    fun formatKopecks(kopecks: Long, currency: String = "₽"): String {
        val rubles = kopecks / 100.0
        return "${rubFormat.format(rubles).replace('\u00A0', ' ')} $currency"
    }

    fun formatRubles(rubles: Double, currency: String = "₽"): String =
        "${rubFormat.format(rubles).replace('\u00A0', ' ')} $currency"

    fun formatShortDate(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }

    fun formatShortDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

    fun formatPeriodLabel(date: LocalDate = LocalDate.now()): String {
        val months = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
        )
        return "${months[date.monthValue - 1]} ${date.year}"
    }
}
