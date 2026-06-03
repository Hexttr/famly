package com.famly.app.domain

val ACCOUNT_ICONS = listOf("💵", "💳", "🏦", "💰", "🧾", "🏠")

const val DEFAULT_ACCOUNT_ICON = "💳"

fun nextAccountIcon(current: String): String {
    val idx = ACCOUNT_ICONS.indexOf(current)
    if (idx == -1) return DEFAULT_ACCOUNT_ICON
    return ACCOUNT_ICONS[(idx + 1) % ACCOUNT_ICONS.size]
}
