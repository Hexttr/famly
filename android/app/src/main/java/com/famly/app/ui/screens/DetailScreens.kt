package com.famly.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.analytics.REPORT_PERIOD_LABELS
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.domain.analytics.filterTransactionsByPeriod
import com.famly.app.domain.analytics.getReportPeriodDescription
import com.famly.app.domain.iconsForType
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.PremiumBg
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Spacing
import kotlinx.coroutines.launch

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
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(cat?.icon ?: "📝", fontSize = 48.sp)
            Text(
                "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "expense") Expense else Income,
            )
            Text(cat?.name ?: "—", style = MaterialTheme.typography.titleMedium)
            if (!tx.note.isNullOrBlank()) {
                Text(tx.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        DetailRow("Дата", MoneyFormatter.formatShortDate(tx.dateEpochDay))
        DetailRow("Счёт", "${acc?.icon ?: ""} ${acc?.name ?: "—"}")
        DetailRow("Повтор", if (tx.isRecurring) "Каждый месяц" else "Нет")
        if (!tx.splitMemberIds.isNullOrBlank()) {
            DetailRow("Split", "Разделено с семьёй")
            TextButton(onClick = onSplit, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Изменить split →", color = Primary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (tx.type == "expense" && state.settings.hasPremiumAccess()) {
            Button(onClick = onSplit, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Разделить с семьёй")
            }
        } else if (tx.type == "expense") {
            Button(onClick = onSplit, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Разделить с семьёй (Премиум)")
            }
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
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.weight(1f))
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
            FamlyCard(modifier = Modifier.padding(top = 16.dp), borderColor = Premium.copy(alpha = 0.4f)) {
                Text(
                    "Премиум: перенос неиспользованного бюджета",
                    color = Premium,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onThemeChange: (String) -> Unit,
    onStartDayChange: (Int) -> Unit,
    onCurrencyChange: (String) -> Unit,
) {
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
                FilterChip(
                    selected = state.settings.theme == "light",
                    onClick = { onThemeChange("light") },
                    label = { Text("☀️ Светлая") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = state.settings.theme == "dark",
                    onClick = { onThemeChange("dark") },
                    label = { Text("🌙 Тёмная") },
                )
            }
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Валюта", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    listOf("RUB" to "₽ Рубль", "USD" to "$ Доллар", "EUR" to "€ Евро").forEach { (code, label) ->
                        FilterChip(
                            selected = state.settings.currency == code,
                            onClick = { onCurrencyChange(code) },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPaywallScreen(state: FamlyUiState, onBack: () -> Unit, onSubscribe: () -> Unit) {
    ScreenScaffold(title = "Премиум", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeroCard(
                gradientStart = Premium,
                gradientEnd = Color(0xFFB8860B),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("⭐", fontSize = 36.sp)
                Text("Famly Премиум", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (state.settings.trialDaysLeft() > 0) {
                    Text(
                        "Пробный период: ${state.settings.trialDaysLeft()} дн.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("199 ₽/мес  ·  1500 ₽/год (−37%)", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            listOf(
                "Семья до 6 человек",
                "Облачная синхронизация",
                "Split + IOU",
                "Перенос остатка бюджета",
                "Расширенная аналитика",
            ).forEach {
                Text("✓ $it", modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Premium),
            ) {
                Text("Оформить Премиум")
            }
            Text(
                "Оплата через RuStore",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PremiumGateScreen(feature: String, onUpgrade: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PremiumGateContent(feature = feature, onUpgrade = onUpgrade)
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
    var categoryId by remember {
        mutableStateOf(state.categories.firstOrNull { it.type == "expense" }?.id ?: "")
    }
    var accountId by remember { mutableStateOf(state.accounts.firstOrNull()?.id ?: "") }
    var note by remember { mutableStateOf("") }
    var recurring by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Новая операция", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                FilterChip(
                    selected = type == "expense",
                    onClick = {
                        type = "expense"
                        categoryId = state.categories.first { it.type == "expense" }.id
                    },
                    label = { Text("Расход") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = type == "income",
                    onClick = {
                        type = "income"
                        categoryId = state.categories.first { it.type == "income" }.id
                    },
                    label = { Text("Доход") },
                )
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", textAlign = TextAlign.Center) },
                textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center),
            )
            Text("Категория", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.categories.filter { it.type == type }) { cat ->
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = { categoryId = cat.id },
                        label = { Text("${cat.icon} ${cat.name}") },
                    )
                }
            }
            Text("Счёт", style = MaterialTheme.typography.labelMedium)
            LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.accounts) { acc ->
                    FilterChip(
                        selected = accountId == acc.id,
                        onClick = { accountId = acc.id },
                        label = { Text("${acc.icon} ${acc.name}") },
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = recurring, onCheckedChange = { recurring = it })
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
fun AccountsScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCycleIcon: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    val total = state.accounts.sumOf { it.balanceKopecks }
    ScreenScaffold(title = "Счета", onBack = onBack) {
        HeroCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Общий баланс", color = Color.White.copy(alpha = 0.85f))
            Text(
                MoneyFormatter.formatKopecks(total),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        state.accounts.forEach { acc ->
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        acc.icon,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clickable { onCycleIcon(acc.id) }
                            .padding(end = 12.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(acc.name, fontWeight = FontWeight.SemiBold)
                        Text(MoneyFormatter.formatKopecks(acc.balanceKopecks))
                    }
                    TextButton(onClick = { onDelete(acc.id) }) { Text("✕") }
                }
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Новый счёт") },
            )
            Button(
                onClick = { if (newName.isNotBlank()) { onAdd(newName); newName = "" } },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("+")
            }
        }
    }
}

@Composable
fun BackupScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onExportJson: suspend () -> String,
    onExportCsv: suspend (ReportPeriod) -> String,
    onExportExcel: suspend (ReportPeriod) -> ByteArray,
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }
    var exporting by remember { mutableStateOf(false) }

    val periodDescription = remember(period) { getReportPeriodDescription(period) }
    val isPremium = state.settings.hasPremiumAccess()

    ScreenScaffold(title = "Backup и экспорт", onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                items(ReportPeriod.entries.toList()) { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(REPORT_PERIOD_LABELS[p] ?: p.name) },
                    )
                }
            }

            ExportOptionButton(
                icon = "📦",
                title = "Сохранить backup (JSON)",
                subtitle = "Все счета, категории и операции",
                enabled = !exporting,
            ) {
                scope.launch {
                    val json = onExportJson()
                    status = "JSON backup готов (${json.length} символов)"
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExportOptionButton(
                icon = "📄",
                title = "Экспорт CSV",
                subtitle = buildExportSubtitle(periodDescription, state, period, isPremium),
                enabled = !exporting,
            ) {
                scope.launch {
                    val csv = onExportCsv(period)
                    val rows = csv.lines().count { it.isNotBlank() } - 1
                    status = "CSV: $rows операций · $periodDescription"
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExportOptionButton(
                icon = "📗",
                title = "Таблица Excel (.xlsx)",
                subtitle = if (exporting) {
                    "Формируем файл…"
                } else {
                    buildExportSubtitle(periodDescription, state, period, isPremium)
                },
                enabled = !exporting,
            ) {
                scope.launch {
                    exporting = true
                    try {
                        val bytes = onExportExcel(period)
                        status = "Excel готов (${bytes.size / 1024} KB) · $periodDescription"
                    } finally {
                        exporting = false
                    }
                }
            }

            if (!isPremium) {
                Text(
                    "Бесплатный тариф: экспорт ограничен последними 30 днями",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            status?.let {
                Text(it, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ExportOptionButton(
    icon: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FamlyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 26.sp, modifier = Modifier.padding(end = 14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text("›", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

private fun buildExportSubtitle(
    periodDescription: String,
    state: FamlyUiState,
    period: ReportPeriod,
    isPremium: Boolean,
): String {
    val txCount = filterTransactionsByPeriod(state.transactions, period).size
    val limitNote = if (!isPremium) " · до 30 дн." else ""
    return "$periodDescription · $txCount операций$limitNote"
}

@Composable
fun CategoriesScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onCycleIcon: (String) -> Unit,
) {
    var tab by remember { mutableStateOf("expense") }
    var newName by remember { mutableStateOf("") }
    ScreenScaffold(title = "Категории", onBack = onBack) {
        Row(modifier = Modifier.padding(16.dp)) {
            FilterChip(
                selected = tab == "expense",
                onClick = { tab = "expense" },
                label = { Text("Расходы") },
                modifier = Modifier.padding(end = 8.dp),
            )
            FilterChip(selected = tab == "income", onClick = { tab = "income" }, label = { Text("Доходы") })
        }
        state.categories.filter { it.type == tab }.forEach { cat ->
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        cat.icon,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable { onCycleIcon(cat.id) }
                            .padding(end = 8.dp),
                    )
                    Text(cat.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    TextButton(onClick = { onDelete(cat.id) }) { Text("✕") }
                }
            }
        }
        Text(
            "Нажмите на иконку для смены · ${iconsForType(tab).size} вариантов",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.weight(1f))
            Button(onClick = { if (newName.isNotBlank()) { onAdd(newName, tab); newName = "" } }) {
                Text("+")
            }
        }
    }
}
