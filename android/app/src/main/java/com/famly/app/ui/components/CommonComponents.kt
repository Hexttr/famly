package com.famly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.SurfaceAlt
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.Warning

@Composable
fun BudgetProgressBar(
    spent: Long,
    limit: Long,
    color: Color = Primary,
    trackColor: Color = SurfaceAlt,
    height: Dp = 8.dp,
    showLabel: Boolean = false,
    label: String? = null,
    labelColor: Color = TextMuted,
    modifier: Modifier = Modifier,
) {
    if (limit <= 0) return
    val progress = (spent.toFloat() / limit).coerceIn(0f, 1f)
    val barColor = when {
        color == Color.White -> color
        progress >= 1f -> Expense
        progress >= 0.9f -> Warning
        else -> color
    }
    val shape = RoundedCornerShape(height / 2)
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(height)
                    .clip(shape)
                    .background(barColor),
            )
        }
        if (showLabel && label != null) {
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        }
    }
}

@Composable
fun CategoryIcon(emoji: String) {
    CategoryEmojiIcon(emoji = emoji)
}
