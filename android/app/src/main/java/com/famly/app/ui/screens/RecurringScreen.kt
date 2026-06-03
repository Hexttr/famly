package com.famly.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.recurring.RecurringProcessor
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.CategoryEmojiIcon
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.SectionHeading
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.parseHexColor

@Composable
fun RecurringScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onDisable: (String) -> Unit,
) {
    val recurring = state.transactions.filter { it.isRecurring }
    ScreenScaffold(onBack = onBack) {
        SectionHeading("🔄", "Периодические платежи")
        Text(
            "Автоматически создаются каждый месяц в выбранный день",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = Spacing.md),
        )
        if (recurring.isEmpty()) {
            FamlyCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Нет периодических операций",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                )
            }
        } else {
            recurring.forEach { tx ->
                RecurringRow(tx, state, onDisable)
            }
        }
    }
}

@Composable
private fun RecurringRow(
    tx: TransactionEntity,
    state: FamlyUiState,
    onDisable: (String) -> Unit,
) {
    val cat = state.categories.find { it.id == tx.categoryId }
    val day = RecurringProcessor.effectiveRecurringDay(tx.recurringDay, tx.dateEpochDay)
    FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryEmojiIcon(
                    emoji = cat?.icon ?: "📦",
                    size = 40.dp,
                    accent = parseHexColor(cat?.color ?: "#457B9D"),
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(cat?.name ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "${MoneyFormatter.formatKopecks(tx.amountKopecks)} · $day-е число",
                        fontSize = 13.sp,
                        color = TextMuted,
                    )
                    Text(
                        "Следующий: ${RecurringProcessor.nextDueLabel(tx)}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text("🔄", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onDisable(tx.id) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Отключить", color = Expense, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
