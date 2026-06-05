package com.famly.app.ui.components

import com.famly.app.ui.theme.FamlyColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.analytics.REPORT_PERIOD_LABELS
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.TextSecondary
import com.famly.app.ui.theme.famlySmShadow

@Composable
fun PeriodFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.md)
    Box(
        modifier = modifier
            .clip(shape)
            .then(if (!selected) Modifier.famlySmShadow(shape) else Modifier)
            .background(if (selected) FamlyColor.primary else MaterialTheme.colorScheme.surface)
            .border(2.dp, if (selected) FamlyColor.primary else FamlyColor.primary.copy(alpha = 0.27f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
fun ReportPeriodRow(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportPeriod.entries.forEach { p ->
            PeriodFilterChip(
                label = REPORT_PERIOD_LABELS[p] ?: "",
                selected = selected == p,
                onClick = { onSelect(p) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun ReportHeroCard(
    periodDescription: String,
    amount: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { ChartBarIcon(Modifier.size(22.dp)) },
    trendBadge: (@Composable () -> Unit)? = null,
) {
    HeroCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    "Расходы за период",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    periodDescription,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    amount,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (trendBadge != null) 28.sp else 30.sp,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = if (trendBadge != null) 8.dp else 0.dp),
                )
                trendBadge?.invoke()
            }
        }
    }
}

@Composable
fun AnalyticsTrendBadge(changePercent: Int?) {
    val bg = when {
        changePercent == null -> Color.White.copy(alpha = 0.16f)
        changePercent > 0 -> Expense.copy(alpha = 0.2f)
        changePercent < 0 -> Color.White.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.16f)
    }
    val label = when {
        changePercent == null -> "нет данных для сравнения"
        changePercent > 0 -> "↑ +$changePercent% к прошлому периоду"
        changePercent < 0 -> "↓ $changePercent% к прошлому периоду"
        else -> "→ 0% к прошлому периоду"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AnalyticsFlowCard(
    label: String,
    amount: String,
    icon: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    FamlyCard(modifier = modifier, borderColor = accent.copy(alpha = 0.35f), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
            Column {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(
                    amount,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    fontSize = 22.sp,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
