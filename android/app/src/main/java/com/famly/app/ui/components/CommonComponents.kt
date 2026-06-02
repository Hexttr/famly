package com.famly.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Warning

@Composable
fun BudgetProgressBar(
    spent: Long,
    limit: Long,
    color: Color = Primary,
    modifier: Modifier = Modifier,
) {
    if (limit <= 0) return
    val progress = (spent.toFloat() / limit).coerceIn(0f, 1f)
    val barColor = when {
        progress >= 1f -> Expense
        progress >= 0.9f -> Warning
        else -> color
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier,
        color = barColor,
        trackColor = Color(0xFFE2E8E5),
    )
}

@Composable
fun CategoryIcon(emoji: String) {
    androidx.compose.material3.Text(text = emoji, modifier = Modifier.size(28.dp))
}
