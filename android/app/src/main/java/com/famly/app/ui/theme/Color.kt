package com.famly.app.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryDark = Color(0xFF1B4332)
val Primary = Color(0xFF2D6A4F)
val PrimaryLight = Color(0xFF40916C)
val Accent = Color(0xFF52B788)
val Background = Color(0xFFF8FAF9)
val Surface = Color(0xFFFFFFFF)
val SurfaceAlt = Color(0xFFF1F5F3)
val TextPrimary = Color(0xFF1A1D1A)
val TextSecondary = Color(0xFF5C6560)
val TextMuted = Color(0xFF8A9390)
val Border = Color(0xFFE2E8E5)
val Expense = Color(0xFFE63946)
val Income = Color(0xFF2D6A4F)
val Warning = Color(0xFFF4A261)
val Premium = Color(0xFFD4A017)
val PremiumBg = Color(0xFFFFF8E7)

val DarkBackground = Color(0xFF121614)
val DarkSurface = Color(0xFF1E2421)
val DarkSurfaceAlt = Color(0xFF2A322E)
val DarkTextPrimary = Color(0xFFF0F4F2)
val DarkTextSecondary = Color(0xFFA8B5AE)
val DarkExpense = Color(0xFFFF6B6B)
val DarkPremium = Color(0xFFFFD166)
val HeroHint = Color(0xFFB7E4C7)

fun parseHexColor(hex: String, fallback: Color = Primary): Color {
    val cleaned = hex.removePrefix("#")
    return runCatching {
        Color(0xFF000000L or cleaned.toLong(16))
    }.getOrDefault(fallback)
}
