package com.famly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.BudgetProgressBar
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.PremiumBg
import com.famly.app.ui.theme.Primary

@Composable
fun HomeScreen(state: FamlyUiState, onOpenOperations: () -> Unit, onOpenTransaction: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Famly", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Text("Привет! 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            val trialDays = state.settings.trialDaysLeft()
            if (state.settings.isPremium) {
                Text("Premium", modifier = Modifier.background(PremiumBg, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp), color = Premium, style = MaterialTheme.typography.labelSmall)
            } else if (trialDays > 0) {
                Text("Trial: $trialDays дн.", modifier = Modifier.background(PremiumBg, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp), color = Premium, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Можно тратить", color = Color.White.copy(0.85f), style = MaterialTheme.typography.labelMedium)
                Text(MoneyFormatter.formatKopecks(state.safeToSpendKopecks), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Потрачено: ${MoneyFormatter.formatKopecks(state.spentKopecks)}", color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                    Text("До конца: ${state.daysLeft} дн.", color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                BudgetProgressBar(spent = state.spentKopecks, limit = state.budgetTotalKopecks, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Доходы", MoneyFormatter.formatKopecks(state.incomeKopecks), Income, Modifier.weight(1f))
            StatCard("Расходы", MoneyFormatter.formatKopecks(state.spentKopecks), Expense, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Последние операции", fontWeight = FontWeight.SemiBold)
            Text("Все →", color = Primary, modifier = Modifier.clickable(onClick = onOpenOperations))
        }
        Spacer(modifier = Modifier.height(8.dp))
        state.transactions.take(5).forEach { tx ->
            val cat = state.categories.find { it.id == tx.categoryId }
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onOpenTransaction(tx.id) }.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(cat?.icon ?: "📝", modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cat?.name ?: "—", fontWeight = FontWeight.Medium)
                    Text(tx.note ?: tx.dateEpochDay.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
                Text(
                    "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
                    fontWeight = FontWeight.SemiBold,
                    color = if (tx.type == "expense") Expense else Income,
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}

@Composable
fun OperationsScreen(state: FamlyUiState, onOpenTransaction: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.transactions, key = { it.id }) { tx ->
            val cat = state.categories.find { it.id == tx.categoryId }
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpenTransaction(tx.id) }) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(cat?.icon ?: "📝", modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat?.name ?: "—", fontWeight = FontWeight.SemiBold)
                        Text("${tx.dateEpochDay}${if (tx.isRecurring) " · 🔄" else ""}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == "expense") Expense else Income,
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetScreen(state: FamlyUiState, onOpenCategory: (String) -> Unit, onOpenCategories: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Бюджет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Категории", color = Primary, modifier = Modifier.clickable(onClick = onOpenCategories))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Общий бюджет: ${MoneyFormatter.formatKopecks(state.budgetTotalKopecks)}")
                Spacer(modifier = Modifier.height(8.dp))
                BudgetProgressBar(spent = state.spentKopecks, limit = state.budgetTotalKopecks)
                Text("Потрачено ${MoneyFormatter.formatKopecks(state.spentKopecks)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        state.categories.filter { it.type == "expense" && it.budgetLimitKopecks != null }.forEach { cat ->
            val spent = state.transactions.filter { it.categoryId == cat.id && it.type == "expense" }.sumOf { it.amountKopecks }
            val limit = cat.budgetLimitKopecks ?: 0L
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onOpenCategory(cat.id) }) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, modifier = Modifier.padding(end = 8.dp))
                        Text(cat.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetProgressBar(spent = spent, limit = limit)
                    Text("${MoneyFormatter.formatKopecks(spent)} / ${MoneyFormatter.formatKopecks(limit)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun MoreScreen(
    state: FamlyUiState,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        "💳" to ("Счета" to "accounts"),
        "📈" to ("Отчёты" to "reports"),
        "⚙️" to ("Настройки" to "settings"),
        "💾" to ("Backup и экспорт" to "backup"),
        "👨‍👩‍👧" to ("Семья" to "family"),
        "⚖️" to ("Балансы IOU" to "balances"),
        "📉" to ("Аналитика" to "analytics"),
        "⭐" to ("Premium" to "premium"),
    )
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ещё", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        items.forEach { (icon, pair) ->
            val (label, route) = pair
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onNavigate(route) },
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, modifier = Modifier.padding(end = 14.dp))
                    Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text("›", color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }
        }
    }
}
