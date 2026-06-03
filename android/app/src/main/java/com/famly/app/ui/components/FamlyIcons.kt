package com.famly.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ChartBarIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.17f, size.height * 0.83f), Offset(size.width * 0.17f, size.height * 0.21f), strokeWidth = 2f)
        drawLine(color, Offset(size.width * 0.17f, size.height * 0.83f), Offset(size.width * 0.92f, size.height * 0.83f), strokeWidth = 2f)
        drawLine(color, Offset(size.width * 0.35f, size.height * 0.83f), Offset(size.width * 0.35f, size.height * 0.46f), strokeWidth = 2.5f)
        drawLine(color, Offset(size.width * 0.52f, size.height * 0.83f), Offset(size.width * 0.52f, size.height * 0.29f), strokeWidth = 2.5f)
        drawLine(color, Offset(size.width * 0.69f, size.height * 0.83f), Offset(size.width * 0.69f, size.height * 0.58f), strokeWidth = 2.5f)
    }
}

@Composable
fun TrendDownIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.15f, h * 0.25f), Offset(w * 0.85f, h * 0.75f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.55f, h * 0.75f), Offset(w * 0.85f, h * 0.75f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.85f, h * 0.45f), Offset(w * 0.85f, h * 0.75f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    }
}

@Composable
fun CategoryListIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Canvas(modifier = modifier) {
        val c = if (color == Color.Unspecified) Color(0xFF2D6A4F) else color
        drawLine(c, Offset(size.width * 0.33f, size.height * 0.25f), Offset(size.width * 0.92f, size.height * 0.25f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(c, Offset(size.width * 0.33f, size.height * 0.5f), Offset(size.width * 0.92f, size.height * 0.5f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(c, Offset(size.width * 0.33f, size.height * 0.75f), Offset(size.width * 0.67f, size.height * 0.75f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawCircle(c, radius = 1.5f, center = Offset(size.width * 0.17f, size.height * 0.25f))
        drawCircle(c, radius = 1.5f, center = Offset(size.width * 0.17f, size.height * 0.5f))
        drawCircle(c, radius = 1.5f, center = Offset(size.width * 0.17f, size.height * 0.75f))
    }
}
