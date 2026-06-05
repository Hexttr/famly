package com.famly.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = Primary.copy(alpha = 0.08f),
    onSecondaryContainer = PrimaryDark,
    tertiary = Premium,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceAlt,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Border,
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.Black,
    primaryContainer = Accent.copy(alpha = 0.15f),
    onPrimaryContainer = DarkTextPrimary,
    secondary = PrimaryLight,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceAlt,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkPremium,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceAlt,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF3A4540),
)

private val PurpleColors = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimary.copy(alpha = 0.14f),
    onPrimaryContainer = PurpleDark,
    secondary = PurpleAccent,
    onSecondary = PurpleDark,
    secondaryContainer = PurpleSurfaceAlt,
    onSecondaryContainer = PurpleDark,
    tertiary = Premium,
    background = PurpleBackground,
    surface = PurpleSurface,
    surfaceVariant = PurpleSurfaceAlt,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = PurpleAccent.copy(alpha = 0.45f),
)

private val BlueColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimary.copy(alpha = 0.14f),
    onPrimaryContainer = BlueDark,
    secondary = BlueAccent,
    onSecondary = BlueDark,
    secondaryContainer = BlueSurfaceAlt,
    onSecondaryContainer = BlueDark,
    tertiary = Premium,
    background = BlueBackground,
    surface = BlueSurface,
    surfaceVariant = BlueSurfaceAlt,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BlueAccent.copy(alpha = 0.45f),
)

private val FamlyTypography = Typography(
    displaySmall = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun FamlyTheme(
    theme: String = "light",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        "dark" -> DarkColors
        "pink" -> PurpleColors
        "blue" -> BlueColors
        "light" -> LightColors
        else -> if (darkTheme) DarkColors else LightColors
    }
    ProvideFamlyPalette(theme = theme) {
        val view = LocalView.current
        val bottomNav = FamlyColor.bottomNav
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.navigationBarColor = bottomNav.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = theme != "dark"
            }
        }
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FamlyTypography,
            content = content,
        )
    }
}
