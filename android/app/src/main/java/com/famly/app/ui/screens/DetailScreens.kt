package com.famly.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.PremiumBg
import com.famly.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(title: String, onBack: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack) { Text("←") }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) { content() }
    }
}

@Composable
fun OperationDetailScreen(
    state: FamlyUiState,
    transactionId: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSplit: () -> Unit,
) {
    val tx = state.transactions.find { it.id == transactionId } ?: return
    val cat = state.categories.find { it.id == tx.categoryId }
    val acc = state.accounts.find { it.id == tx.accountId }

    ScreenScaffold(title = "Операция", onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(cat?.icon ?: "📝", fontSize = 48.sp)
            Text(
                "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "expense") Expense else Income,
            )
            Text(cat?.name ?: "—", style = MaterialTheme.typography.titleMedium)
        }
        DetailRow("Дата", tx.dateEpochDay.toString())
        DetailRow("Счёт", "${acc?.icon} ${acc?.name}")
        DetailRow("Повтор", if (tx.isRecurring) "Каждый месяц" else "Нет")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSplit, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Разделить с семьёй (Premium)")
        }
        Button(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Expense.copy(alpha = 0.15f), contentColor = Expense),
        ) {
            Text("Удалить")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CategoryBudgetScreen(state: FamlyUiState, categoryId: String, onBack: () -> Unit, onSaveLimit: (Long) -> Unit) {
    val cat = state.categories.find { it.id == categoryId } ?: return
    var limit by remember(cat) { mutableStateOf(((cat.budgetLimitKopecks ?: 0) / 100).toString()) }

    ScreenScaffold(title = cat.name, onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it; onSaveLimit(it.toLongOrNull() ?: 0) },
                label = { Text("Лимит бюджета (₽)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PremiumBg)) {
                Text("Premium: Rollover неиспользованного бюджета", modifier = Modifier.padding(16.dp), color = Premium, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsScreen(state: FamlyUiState, onBack: () -> Unit, onThemeChange: (String) -> Unit, onStartDayChange: (Int) -> Unit) {
    ScreenScaffold(title = "Настройки", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Начало периода (день)", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                (1..28).forEach { day ->
                    if (day == state.settings.budgetPeriod.startDay || day % 7 == 0 || day == 1 || day == 28) {
                        FilterChip(
                            selected = state.settings.budgetPeriod.startDay == day,
                            onClick = { onStartDayChange(day) },
                            label = { Text("$day") },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                }
            }
            Text("Тема", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp))
            Row {
                FilterChip(selected = state.settings.theme == "light", onClick = { onThemeChange("light") }, label = { Text("☀️ Светлая") }, modifier = Modifier.padding(end = 8.dp))
                FilterChip(selected = state.settings.theme == "dark", onClick = { onThemeChange("dark") }, label = { Text("🌙 Тёмная") })
            }
        }
    }
}

@Composable
fun PremiumPaywallScreen(state: FamlyUiState, onBack: () -> Unit, onSubscribe: () -> Unit) {
    ScreenScaffold(title = "Premium", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⭐", fontSize = 48.sp)
            Text("Famly Premium", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (state.settings.trialDaysLeft() > 0) {
                Text("Trial: ${state.settings.trialDaysLeft()} дн.", color = Premium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("199 ₽/мес  ·  1500 ₽/год (−37%)", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            listOf("Семья до 6 человек", "Облачная sync", "Split + IOU", "Rollover", "Аналитика").forEach {
                Text("✓ $it", modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onSubscribe, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Premium)) {
                Text("Оформить Premium")
            }
            Text("Оплата через RuStore", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun PremiumGateScreen(feature: String, onUpgrade: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text("⭐", fontSize = 40.sp)
        Text("Premium", fontWeight = FontWeight.Bold)
        Text("$feature доступна в Premium", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
        Button(onClick = onUpgrade, colors = ButtonDefaults.buttonColors(containerColor = Premium)) { Text("Попробовать Premium") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    state: FamlyUiState,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: String, type: String, categoryId: String, accountId: String, note: String, recurring: Boolean) -> Unit,
) {
    if (!visible) return
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var categoryId by remember { mutableStateOf(state.categories.firstOrNull { it.type == "expense" }?.id ?: "") }
    var accountId by remember { mutableStateOf(state.accounts.firstOrNull()?.id ?: "") }
    var note by remember { mutableStateOf("") }
    var recurring by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Новая операция", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                FilterChip(selected = type == "expense", onClick = { type = "expense"; categoryId = state.categories.first { it.type == "expense" }.id }, label = { Text("Расход") }, modifier = Modifier.padding(end = 8.dp))
                FilterChip(selected = type == "income", onClick = { type = "income"; categoryId = state.categories.first { it.type == "income" }.id }, label = { Text("Доход") })
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center))
            LazyColumn(modifier = Modifier.height(120.dp).padding(vertical = 8.dp)) {
                items(state.categories.filter { it.type == type }) { cat ->
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = { categoryId = cat.id },
                        label = { Text("${cat.icon} ${cat.name}") },
                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                    )
                }
            }
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Checkbox(checked = recurring, onCheckedChange = { recurring = it })
                Text("Повторять каждый месяц")
            }
            Button(
                onClick = {
                    onSave(amount, type, categoryId, accountId, note, recurring)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("Сохранить")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountsScreen(state: FamlyUiState, onBack: () -> Unit, onAdd: (String) -> Unit, onDelete: (String) -> Unit) {
    var newName by remember { mutableStateOf("") }
    ScreenScaffold(title = "Счета", onBack = onBack) {
        val total = state.accounts.sumOf { it.balanceKopecks }
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Primary)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Общий баланс", color = Color.White.copy(0.85f))
                Text(MoneyFormatter.formatKopecks(total), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            }
        }
        state.accounts.forEach { acc ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(acc.icon, modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(acc.name, fontWeight = FontWeight.SemiBold)
                        Text(MoneyFormatter.formatKopecks(acc.balanceKopecks))
                    }
                    TextButton(onClick = { onDelete(acc.id) }) { Text("✕") }
                }
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Новый счёт") })
            Button(onClick = { if (newName.isNotBlank()) { onAdd(newName); newName = "" } }, modifier = Modifier.padding(start = 8.dp)) { Text("+") }
        }
    }
}

@Composable
fun ReportsScreen(state: FamlyUiState, onBack: () -> Unit) {
    ScreenScaffold(title = "Отчёты", onBack = onBack) {
        val expenses = state.categories.filter { it.type == "expense" }.map { cat ->
            cat to state.transactions.filter { it.categoryId == cat.id && it.type == "expense" }.sumOf { it.amountKopecks }
        }.sortedByDescending { it.second }.take(5)
        val total = expenses.sumOf { it.second }
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Расходы: ${MoneyFormatter.formatKopecks(total)}")
            expenses.forEach { (cat, spent) ->
                val pct = if (total > 0) (spent * 100 / total) else 0
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(cat.icon, modifier = Modifier.padding(end = 8.dp))
                    Text(cat.name, modifier = Modifier.weight(1f))
                    Text(MoneyFormatter.formatKopecks(spent), fontWeight = FontWeight.SemiBold)
                    Text(" $pct%", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        }
    }
}

@Composable
fun BackupScreen(onBack: () -> Unit) {
    ScreenScaffold(title = "Backup и экспорт", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("📦 Сохранить backup (JSON)") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("📊 Экспорт CSV (30 дней)") }
        }
    }
}

@Composable
fun CategoriesScreen(state: FamlyUiState, onBack: () -> Unit, onAdd: (String, String) -> Unit, onDelete: (String) -> Unit) {
    var tab by remember { mutableStateOf("expense") }
    var newName by remember { mutableStateOf("") }
    ScreenScaffold(title = "Категории", onBack = onBack) {
        Row(modifier = Modifier.padding(16.dp)) {
            FilterChip(selected = tab == "expense", onClick = { tab = "expense" }, label = { Text("Расходы") }, modifier = Modifier.padding(end = 8.dp))
            FilterChip(selected = tab == "income", onClick = { tab = "income" }, label = { Text("Доходы") })
        }
        state.categories.filter { it.type == tab }.forEach { cat ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(cat.icon, modifier = Modifier.padding(end = 8.dp))
                Text(cat.name, modifier = Modifier.weight(1f))
                TextButton(onClick = { onDelete(cat.id) }) { Text("✕") }
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.weight(1f))
            Button(onClick = { if (newName.isNotBlank()) { onAdd(newName, tab); newName = "" } }) { Text("+") }
        }
    }
}

@Composable
fun FamilyScreen(state: FamlyUiState, onBack: () -> Unit, onUpgrade: () -> Unit) {
    if (!state.settings.hasPremiumAccess()) {
        PremiumGateScreen("Семейный бюджет", onUpgrade)
        return
    }
    ScreenScaffold(title = "Семья", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Участников: 3 / 6", fontWeight = FontWeight.Bold)
            listOf("👨 Алексей — Админ", "👩 Мария — Участник", "👦 Саша — Наблюдатель").forEach {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(it, modifier = Modifier.padding(16.dp))
                }
            }
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("+ Пригласить") }
        }
    }
}

@Composable
fun BalancesScreen(state: FamlyUiState, onBack: () -> Unit, onUpgrade: () -> Unit) {
    if (!state.settings.hasPremiumAccess()) {
        PremiumGateScreen("Балансы IOU", onUpgrade)
        return
    }
    ScreenScaffold(title = "Кто кому должен", onBack = onBack) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Мария должна Алексею")
                Text("1 250 ₽", fontWeight = FontWeight.Bold, color = Expense)
                Button(onClick = { }, modifier = Modifier.padding(top = 8.dp)) { Text("Закрыть долг") }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(state: FamlyUiState, onBack: () -> Unit, onUpgrade: () -> Unit) {
    if (!state.settings.hasPremiumAccess()) {
        PremiumGateScreen("Расширенная аналитика", onUpgrade)
        return
    }
    ScreenScaffold(title = "Аналитика", onBack = onBack) {
        Text("Тренд 3 месяцев · +8% к прошлому периоду", modifier = Modifier.padding(16.dp))
    }
}
