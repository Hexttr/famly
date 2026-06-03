package com.famly.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.famly.app.ui.components.BudgetProgressBar
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.FamlyFilterChip
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.components.SectionHeading
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.parseHexColor
import kotlin.math.min

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

    ScreenScaffold(onBack = onBack) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPeriod.entries.forEach { p ->
                    FamlyFilterChip(
                        label = REPORT_PERIOD_LABELS[p] ?: "",
                        selected = period == p,
                        onClick = { period = p },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(periodDescription, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    Text(
                        MoneyFormatter.formatKopecks(totalSpent),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("расходы за период", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            if (totalSpent > 0 && top5.isNotEmpty()) {
                FamlyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DonutChart(
                            slices = top5.map { it.first.color to it.second },
                            total = totalSpent,
                            modifier = Modifier.size(120.dp),
                        )
                        Column(modifier = Modifier.padding(start = Spacing.md)) {
                            top5.forEach { (cat, spent) ->
                                val pct = if (totalSpent > 0) (spent * 100 / totalSpent) else 0
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(cat.icon, modifier = Modifier.padding(end = 6.dp))
                                    Text(cat.name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                    Text("$pct%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
            SectionHeading("📊", "Топ-5 категорий")
            top5.forEach { (cat, spent) ->
                val pct = if (totalSpent > 0) (spent * 100 / totalSpent) else 0
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, modifier = Modifier.padding(end = 8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.name, fontWeight = FontWeight.SemiBold)
                            BudgetProgressBar(
                                spent = spent,
                                limit = totalSpent.coerceAtLeast(1),
                                color = parseHexColor(cat.color),
                                height = 6.dp,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(MoneyFormatter.formatKopecks(spent), fontWeight = FontWeight.Bold)
                            Text("$pct%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Pair<String, Long>>,
    total: Long,
    modifier: Modifier = Modifier,
) {
    val colors = slices.map { (colorHex, _) ->
        runCatching { parseHexColor(colorHex) }.getOrElse { Primary }
    }
    Canvas(modifier = modifier) {
        var startAngle = -90f
        if (total <= 0) {
            drawArc(color = Color(0xFFE2E8E5), startAngle = 0f, sweepAngle = 360f, useCenter = false)
            return@Canvas
        }
        slices.forEachIndexed { i, (_, amount) ->
            val sweep = (amount.toFloat() / total) * 360f
            drawArc(
                color = colors.getOrElse(i) { Primary },
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.22f, cap = StrokeCap.Butt),
            )
            startAngle += sweep
        }
    }
}

@Composable
fun AnalyticsScreen(state: FamlyUiState, onBack: () -> Unit, onUpgrade: () -> Unit) {
    if (!state.settings.hasPremiumAccess()) {
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
        getBudgetWarnings(state.categories, state.transactions)
    }
    val filteredTx = remember(state.transactions, period) {
        filterTransactionsByPeriod(state.transactions, period)
    }
    val avgDaily = getAverageDailyExpense(state.transactions, period)
    val income = getTotalIncome(filteredTx)
    val expenses = getTotalExpenses(filteredTx)

    ScreenScaffold(onBack = onBack) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPeriod.entries.forEach { p ->
                    FamlyFilterChip(
                        label = REPORT_PERIOD_LABELS[p] ?: "",
                        selected = period == p,
                        onClick = { period = p },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(getReportPeriodDescription(period), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowStatCard("Расходы", MoneyFormatter.formatKopecks(expenses), "📉", Expense, Modifier.weight(1f))
                FlowStatCard("Доходы", MoneyFormatter.formatKopecks(income), "📈", Income, Modifier.weight(1f))
            }
            comparison.changePercent?.let { change ->
                val sign = if (change > 0) "+" else ""
                Text(
                    "К прошлому периоду: $sign$change% расходов",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
            }
            Text("Средний расход в день: ${MoneyFormatter.formatKopecks(avgDaily)}", modifier = Modifier.padding(bottom = Spacing.md))

            SectionHeading("📅", "По месяцам")
            monthly.forEach { m ->
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Text(m.label, fontWeight = FontWeight.Bold)
                    Text("Расходы: ${MoneyFormatter.formatKopecks(m.expensesKopecks)}")
                    Text("Доходы: ${MoneyFormatter.formatKopecks(m.incomeKopecks)}", color = Income)
                }
            }

            SectionHeading("📊", "Тренды категорий")
            trends.take(8).forEach { trend ->
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(trend.icon, modifier = Modifier.padding(end = 8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trend.name, fontWeight = FontWeight.SemiBold)
                            Text(MoneyFormatter.formatKopecks(trend.currentKopecks), fontSize = 13.sp)
                        }
                        trend.changePercent?.let { c ->
                            val color = if (c > 0) Expense else Income
                            Text("${if (c > 0) "+" else ""}$c%", color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (warnings.isNotEmpty()) {
                SectionHeading("⚠️", "Предупреждения бюджета")
                warnings.forEach { w ->
                    val cat = state.categories.find { it.id == w.categoryId }
                    FamlyCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                        borderColor = Expense.copy(alpha = 0.4f),
                    ) {
                        Text("${cat?.icon} ${cat?.name}: ${(w.percent * 100).toInt()}% лимита")
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowStatCard(label: String, amount: String, icon: String, accent: Color, modifier: Modifier = Modifier) {
    FamlyCard(modifier = modifier, borderColor = accent.copy(alpha = 0.35f)) {
        Text(icon, fontSize = 22.sp)
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(amount, fontWeight = FontWeight.Bold, color = accent, fontSize = 18.sp)
    }
}
