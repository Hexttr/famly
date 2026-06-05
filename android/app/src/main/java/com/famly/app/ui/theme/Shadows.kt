package com.famly.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.famlySmShadow(shape: Shape = RoundedCornerShape(Radius.md)): Modifier =
    shadow(
        elevation = 1.dp,
        shape = shape,
        ambientColor = Color(0x0F000000),
        spotColor = Color(0x0F000000),
    )

fun Modifier.famlyCardShadow(shape: Shape = RoundedCornerShape(Radius.lg)): Modifier =
    shadow(
        elevation = 2.dp,
        shape = shape,
        ambientColor = Color(0x0F1B4332),
        spotColor = Color(0x141B4332),
    )

fun Modifier.famlyHeroShadow(shape: Shape = RoundedCornerShape(Radius.xl)): Modifier =
    shadow(
        elevation = 8.dp,
        shape = shape,
        ambientColor = Color(0x471B4332),
        spotColor = Color(0x471B4332),
    )

fun Modifier.famlyHeaderButtonShadow(primary: Boolean = false): Modifier =
    shadow(
        elevation = if (primary) 4.dp else 2.dp,
        shape = RoundedCornerShape(50),
        ambientColor = if (primary) Color(0x592D6A4F) else Color(0x2E2D6A4F),
        spotColor = if (primary) Color(0x592D6A4F) else Color(0x2E2D6A4F),
    )

fun Modifier.famlyBottomNavShadow(): Modifier =
    shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
        ambientColor = Color(0x591B4332),
        spotColor = Color(0x591B4332),
    )

fun Modifier.famlyFloatingNavShadow(shape: Shape = RoundedCornerShape(28.dp)): Modifier =
    shadow(
        elevation = 16.dp,
        shape = shape,
        ambientColor = Color(0x661B4332),
        spotColor = Color(0x802D6A4F),
    )

fun Modifier.famlyFabShadow(): Modifier =
    shadow(
        elevation = 10.dp,
        shape = CircleShape,
        ambientColor = Color(0x8040916C),
        spotColor = Color(0x992D6A4F),
    )
