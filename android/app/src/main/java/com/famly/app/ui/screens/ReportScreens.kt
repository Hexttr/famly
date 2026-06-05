package com.famly.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.FamlyAccess
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.analytics.REPORT_PERIOD_LABELS
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.domain.analytics.filterTransactionsByPeriod
import com.famly.app.domain.analytics.getAverageDailyExpense
import com.famly.app.domain.analytics.getBudgetWarnings
import com.famly.app.domain.analytics.getCategoryExpenseTrends
import com.famly.app.domain.analytics.getCategorySpent
import com.famly.app.domain.analytics.getMonthlyTotals
import com.famly.app.domain.analytics.getMonthsCountForPeriod
import com.famly.app.domain.analytics.getPeriodExpenseComparison
import com.famly.app.domain.analytics.getReportPeriodDescription
import com.famly.app.domain.analytics.getTotalExpenses
import com.famly.app.domain.analytics.getTotalIncome
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.CategoryEmojiIcon
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.AnalyticsFlowCard
import com.famly.app.ui.components.AnalyticsTrendBadge
import com.famly.app.ui.components.ChartBarIcon
import com.famly.app.ui.components.ReportHeroCard
import com.famly.app.ui.components.ReportPeriodRow
import com.famly.app.ui.components.TrendDownIcon
import com.famly.app.ui.components.GroupedListCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.components.SectionHeading
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.PrimaryLight
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.TextSecondary
import com.famly.app.ui.theme.parseHexColor

@Composable
fun ReportsScreen(state: FamlyUiState, onBack: () -> Unit) {
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }
    val filteredTx = remember(state.transactions, period) {
        filterTransactionsByPeriod(state.transactions, period)
    }
    val expenseCategories = remember(state.categories, filteredTx) {
        state.categories
            .filter { it.type == "expense" }
            .map { it to getCategorySpent(it.id, filteredTx) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }
    val totalSpent = expenseCategories.sumOf { it.second }
    val top5 = expenseCategories.take(5)
    val periodDescription = getReportPeriodDescription(period)
    val topShare = if (totalSpent > 0 && top5.isNotEmpty()) {
        (top5.first().second * 100 / totalSpent).toInt()
    } else {
        0
    }

    ScreenScaffold(onBack = onBack) {
        ReportPeriodRow(selected = period, onSelect = { period = it })
        Spacer(modifier = Modifier.height(Spacing.md))
        ReportHeroCard(
            periodDescription = periodDescription,
            amount = MoneyFormatter.formatKopecks(totalSpent),
            modifier = Modifier.fillMaxWidth(),
            icon = { ChartBarIcon(Modifier.size(22.dp)) },
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        if (totalSpent > 0 && top5.isNotEmpty()) {
            FamlyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DonutChartWithCenter(
                        slices = top5.map { parseHexColor(it.first.color) to it.second },
                        total = totalSpent,
                        centerTitle = top5.first().first.name,
                        centerPercent = topShare,
                        modifier = Modifier.size(200.dp),
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    top5.forEach { (cat, amount) ->
                        val pct = if (totalSpent > 0) (amount * 100 / totalSpent).toInt() else 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(cat.color)),
                            )
                            Text(
                                cat.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                MoneyFormatter.formatKopecks(amount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "$pct%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        SectionHeading("📊", "Топ-5 категорий", modifier = Modifier.padding(top = Spacing.md))
        if (top5.isEmpty()) {
            FamlyCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Нет расходов за выбранный период",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                )
            }
        } else {
            GroupedListCard(modifier = Modifier.fillMaxWidth()) {
                top5.forEachIndexed { index, (cat, spent) ->
                    val pct = if (totalSpent > 0) (spent * 100 / totalSpent).toInt() else 0
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryEmojiIcon(emoji = cat.icon, size = 36.dp, accent = parseHexColor(cat.color))
                            Text(
                                cat.name,
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                            Text(MoneyFormatter.formatKopecks(spent), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "$pct%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(parseHexColor(cat.color)),
                            )
                        }
                    }
                    if (index < top5.lastIndex) {
                        HorizontalDivider(color = Primary.copy(alpha = 0.12f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChartWithCenter(
    slices: List<Pair<Color, Long>>,
    total: Long,
    centerTitle: String,
    centerPercent: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            var startAngle = -90f
            if (total <= 0) {
                drawArc(color = Color(0xFFE2E8E5), startAngle = 0f, sweepAngle = 360f, useCenter = false)
                return@Canvas
            }
            slices.forEach { (color, amount) ->
                val sweep = (amount.toFloat() / total) * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = size.minDimension * 0.22f,
                        cap = StrokeCap.Butt,
                    ),
                )
                startAngle += sweep
            }
        }
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, Primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$centerPercent%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun AnalyticsScreen(state: FamlyUiState, onBack: () -> Unit, onUpgrade: () -> Unit) {
    if (!FamlyAccess.hasPremium(state.settings)) {
        ScreenScaffold(onBack = onBack) {
            PremiumGateContent("Расширенная аналитика", onUpgrade, modifier = Modifier.fillMaxWidth())
        }
        return
    }
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }
    val comparison = remember(state.transactions, period) {
        getPeriodExpenseComparison(state.transactions, period)
    }
    val monthly = remember(state.transactions, period) {
        getMonthlyTotals(state.transactions, getMonthsCountForPeriod(period))
    }
    val trends = remember(state.categories, state.transactions, period) {
        getCategoryExpenseTrends(state.categories, state.transactions, period)
    }
    val warnings = remember(state.categories, state.transactions) {
        getBudgetWarnings(state.categories, state.transactions, threshold = 0.75f)
    }
    val filteredTx = remember(state.transactions, period) {
        filterTransactionsByPeriod(state.transactions, period)
    }
    val avgDaily = getAverageDailyExpense(state.transactions, period)
    val income = getTotalIncome(filteredTx)
    val expenses = getTotalExpenses(filteredTx)
    val netBalance = income - expenses
    val periodDescription = getReportPeriodDescription(period)
    val maxMonthlyExpense = (monthly.maxOfOrNull { it.expensesKopecks } ?: 1L).coerceAtLeast(1L)

    val insights = buildList {
        if (avgDaily > 0) add("Средний расход в день — ${MoneyFormatter.formatKopecks(avgDaily)}")
        if (netBalance >= 0 && income > 0) {
            add("Профицит за период — ${MoneyFormatter.formatKopecks(netBalance)}")
        } else if (netBalance < 0) {
            add("Расходы превышают доходы на ${MoneyFormatter.formatKopecks(-netBalance)}")
        }
        trends.find { (it.changePercent ?: 0) > 0 && it.currentKopecks > 0 }?.let { topGrowth ->
            topGrowth.changePercent?.let { add("Рост расходов: «${topGrowth.name}» +$it%") }
        }
        trends.find { (it.changePercent ?: 0) < 0 }?.let { topDrop ->
            topDrop.changePercent?.let { add("Снижение: «${topDrop.name}» $it%") }
        }
        warnings.firstOrNull()?.let { w ->
            state.categories.find { it.id == w.categoryId }?.let { cat ->
                add("«${cat.name}» — ${(w.percent * 100).toInt()}% от лимита бюджета")
            }
        }
    }

    ScreenScaffold(onBack = onBack) {
        Column {
            ReportPeriodRow(selected = period, onSelect = { period = it })
            Spacer(modifier = Modifier.height(Spacing.md))
            ReportHeroCard(
                periodDescription = periodDescription,
                amount = MoneyFormatter.formatKopecks(comparison.currentExpensesKopecks),
                modifier = Modifier.fillMaxWidth(),
                icon = { TrendDownIcon(Modifier.size(28.dp)) },
                trendBadge = { AnalyticsTrendBadge(comparison.changePercent) },
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsFlowCard("Доходы", MoneyFormatter.formatKopecks(income), "💰", Income, Modifier.weight(1f))
                AnalyticsFlowCard("Расходы", MoneyFormatter.formatKopecks(expenses), "💸", Expense, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            SectionHeading("📊", "Динамика по месяцам")
            FamlyCard(modifier = Modifier.fillMaxWidth(), padding = 18.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        monthly.forEach { m ->
                            val heightPct = if (m.expensesKopecks > 0) {
                                (m.expensesKopecks.toFloat() / maxMonthlyExpense * 100f).coerceAtLeast(8f)
                            } else {
                                4f
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    if (m.expensesKopecks > 0) {
                                        MoneyFormatter.formatKopecks(m.expensesKopecks).replace(" ₽", "")
                                    } else {
                                        "—"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.height(24.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.72f)
                                        .height(100.dp),
                                    contentAlignment = Alignment.BottomCenter,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(heightPct / 100f)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                            .then(
                                                if (m.expensesKopecks == 0L) {
                                                    Modifier
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(2.dp, Primary.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                                } else {
                                                    Modifier.background(Brush.verticalGradient(listOf(PrimaryLight, Primary)))
                                                },
                                            ),
                                    )
                                }
                                Text(m.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = Primary.copy(alpha = 0.12f),
                    )
                }
            }
            if (insights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                SectionHeading("💡", "Выводы")
                GroupedListCard(modifier = Modifier.fillMaxWidth()) {
                    insights.forEachIndexed { index, text ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                            )
                            Text(text, modifier = Modifier.padding(start = 10.dp), fontSize = 14.sp, lineHeight = 20.sp)
                        }
                        if (index < insights.lastIndex) {
                            HorizontalDivider(color = Primary.copy(alpha = 0.12f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            SectionHeading("📈", "Изменение по категориям")
            if (trends.isEmpty()) {
                FamlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Недостаточно данных для сравнения",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        color = TextMuted,
                    )
                }
            } else {
                GroupedListCard(modifier = Modifier.fillMaxWidth()) {
                    trends.take(5).forEachIndexed { index, trend ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryEmojiIcon(emoji = trend.icon, size = 36.dp, accent = parseHexColor(trend.color))
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(trend.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    "${MoneyFormatter.formatKopecks(trend.currentKopecks)} · было ${MoneyFormatter.formatKopecks(trend.previousKopecks)}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Text(
                                formatChangePercent(trend.changePercent),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when {
                                    trend.changePercent == null -> TextMuted
                                    trend.changePercent > 0 -> Expense
                                    trend.changePercent < 0 -> Income
                                    else -> TextSecondary
                                },
                            )
                        }
                        if (index < minOf(4, trends.lastIndex)) {
                            HorizontalDivider(color = Primary.copy(alpha = 0.12f))
                        }
                    }
                }
            }
            if (warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                SectionHeading("⚠️", "Предупреждения бюджета")
                warnings.forEach { w ->
                    val cat = state.categories.find { it.id == w.categoryId }
                    FamlyCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                        borderColor = Expense.copy(alpha = 0.35f),
                    ) {
                        Text(
                            "${cat?.icon} ${cat?.name}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                        Text(
                            "${(w.percent * 100).toInt()}% от лимита бюджета",
                            color = Expense,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatChangePercent(value: Int?): String = when {
    value == null -> "—"
    value > 0 -> "+$value%"
    value < 0 -> "$value%"
    else -> "0%"
}
