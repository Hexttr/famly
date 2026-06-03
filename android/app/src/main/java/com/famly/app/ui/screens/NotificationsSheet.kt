package com.famly.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.famly.app.domain.analytics.getBudgetWarnings
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    state: FamlyUiState,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val warnings = remember(state.categories, state.transactions) {
        getBudgetWarnings(state.categories, state.transactions, threshold = 0.75f)
    }
    val hasTrialNotice = state.settings.trialDaysLeft() > 0 && !state.settings.isPremium
    val hasBudgetLow = state.safeToSpendKopecks <= state.budgetTotalKopecks / 10 && state.budgetTotalKopecks > 0
    val hasAnyNotice = hasTrialNotice || hasBudgetLow || warnings.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.lg)) {
            Text("Уведомления", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Бюджет · ${state.periodLabel}",
                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.md),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            if (hasTrialNotice) {
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Text("⭐ Пробный Премиум", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Осталось ${state.settings.trialDaysLeft()} дн. · семья, split, аналитика",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            warnings.forEach { w ->
                val cat = state.categories.find { it.id == w.categoryId } ?: return@forEach
                val pct = (w.percent * 100).toInt()
                val title = when {
                    pct >= 100 -> "⚠️ Превышен лимит: ${cat.name}"
                    pct >= 90 -> "⚠️ Опасная зона: ${cat.name}"
                    else -> "⚠️ Близко к лимиту: ${cat.name}"
                }
                val subtitle = when {
                    pct >= 100 -> "$pct% от лимита бюджета · расходы выше плана"
                    pct >= 90 -> "$pct% от лимита · осталось мало запаса"
                    else -> "$pct% от лимита бюджета за период"
                }
                FamlyCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                    borderColor = Expense.copy(alpha = 0.35f),
                ) {
                    Text(title, fontWeight = FontWeight.SemiBold, color = Expense)
                    Text(
                        "${cat.icon} $subtitle",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (hasBudgetLow) {
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Text("⚠️ Бюджет на исходе", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Осталось мало средств до конца периода",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (!hasAnyNotice) {
                FamlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Пока всё спокойно", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Здесь будут напоминания о бюджете, семье и повторяющихся операциях",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
