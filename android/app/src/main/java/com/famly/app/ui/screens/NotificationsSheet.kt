package com.famly.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.famly.app.domain.FamlyAccess
import com.famly.app.domain.analytics.getBudgetWarnings
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted

private data class NoticeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    state: FamlyUiState,
    visible: Boolean,
    onDismiss: () -> Unit,
    onDismissNotice: (String) -> Unit,
) {
    if (!visible) return
    val dismissed = state.settings.dismissedNotificationIds
    val periodKey = state.periodLabel
    val warnings = remember(state.categories, state.transactions, periodKey) {
        getBudgetWarnings(state.categories, state.transactions, threshold = 0.75f)
    }
    val notices = buildList {
        if (FamlyAccess.showPaywall() && state.settings.trialDaysLeft() > 0 && !state.settings.isPremium) {
            add(
                NoticeItem(
                    id = "trial",
                    title = "Пробный период",
                    subtitle = "Осталось ${state.settings.trialDaysLeft()} дн.",
                ),
            )
        }
        warnings.forEach { w ->
            val cat = state.categories.find { it.id == w.categoryId } ?: return@forEach
            val pct = (w.percent * 100).toInt()
            val title = when {
                pct >= 100 -> "Превышен лимит: ${cat.name}"
                pct >= 90 -> "Опасная зона: ${cat.name}"
                else -> "Близко к лимиту: ${cat.name}"
            }
            add(
                NoticeItem(
                    id = "budget_${cat.id}_$periodKey",
                    title = title,
                    subtitle = "${cat.icon} $pct% от лимита бюджета",
                    accent = true,
                ),
            )
        }
        if (state.safeToSpendKopecks <= state.budgetTotalKopecks / 10 && state.budgetTotalKopecks > 0) {
            add(
                NoticeItem(
                    id = "budget_low_$periodKey",
                    title = "Бюджет на исходе",
                    subtitle = "Осталось мало средств до конца периода",
                    accent = true,
                ),
            )
        }
    }.filter { it.id !in dismissed }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Expense)
                Text(
                    "Уведомления",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }
            Text(
                "Бюджет · ${state.periodLabel}",
                modifier = Modifier.padding(bottom = Spacing.md),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            if (notices.isEmpty()) {
                FamlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Пока всё спокойно", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Здесь будут напоминания о бюджете и повторяющихся операциях",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                notices.forEach { notice ->
                    FamlyCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                        borderColor = if (notice.accent) Expense.copy(alpha = 0.35f) else Primary.copy(alpha = 0.2f),
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    notice.title,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (notice.accent) Expense else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    notice.subtitle,
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            IconButton(onClick = { onDismissNotice(notice.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Скрыть", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
