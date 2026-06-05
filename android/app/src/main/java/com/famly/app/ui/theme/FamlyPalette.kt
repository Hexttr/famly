package com.famly.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FamlyPalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val accent: Color,
    val income: Color,
    val heroHint: Color,
    val bottomNav: Color,
)

val LightPalette = FamlyPalette(
    primary = Primary,
    primaryDark = PrimaryDark,
    primaryLight = PrimaryLight,
    accent = Accent,
    income = Income,
    heroHint = HeroHint,
    bottomNav = BottomNavBackground,
)

val DarkPalette = FamlyPalette(
    primary = Accent,
    primaryDark = PrimaryDark,
    primaryLight = PrimaryLight,
    accent = PrimaryLight,
    income = Accent,
    heroHint = HeroHint,
    bottomNav = BottomNavBackground,
)

/** Feminine theme — purple accents instead of green. */
val PurplePalette = FamlyPalette(
    primary = PurplePrimary,
    primaryDark = PurpleDark,
    primaryLight = PurpleLight,
    accent = PurpleAccent,
    income = PurpleIncome,
    heroHint = PurpleHeroHint,
    bottomNav = PurpleBottomNav,
)

/** Masculine theme — blue accents instead of green. */
val BluePalette = FamlyPalette(
    primary = BluePrimary,
    primaryDark = BlueDark,
    primaryLight = BlueLight,
    accent = BlueAccent,
    income = BlueIncome,
    heroHint = BlueHeroHint,
    bottomNav = BlueBottomNav,
)

val LocalFamlyPalette = staticCompositionLocalOf { LightPalette }

object FamlyColor {
    val primary: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.primary

    val primaryDark: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.primaryDark

    val primaryLight: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.primaryLight

    val accent: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.accent

    val income: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.income

    val heroHint: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.heroHint

    val bottomNav: Color
        @Composable @ReadOnlyComposable
        get() = LocalFamlyPalette.current.bottomNav

    /** Card/chip border tint — follows theme primary. */
    val border: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline
}

@Composable
fun ProvideFamlyPalette(theme: String, content: @Composable () -> Unit) {
    val palette = when (theme) {
        "dark" -> DarkPalette
        "pink" -> PurplePalette
        "blue" -> BluePalette
        else -> LightPalette
    }
    CompositionLocalProvider(LocalFamlyPalette provides palette, content = content)
}
