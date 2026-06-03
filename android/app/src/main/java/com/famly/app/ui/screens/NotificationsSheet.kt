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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
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
            if (state.settings.trialDaysLeft() > 0 && !state.settings.isPremium) {
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
            if (state.safeToSpendKopecks <= state.budgetTotalKopecks / 10) {
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
