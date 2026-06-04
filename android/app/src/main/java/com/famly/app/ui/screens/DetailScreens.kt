package com.famly.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.famly.app.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.FamlyAccess
import com.famly.app.domain.budget.BudgetRolloverProcessor
import com.famly.app.domain.DEFAULT_EXPENSE_ICON
import com.famly.app.domain.DEFAULT_INCOME_ICON
import com.famly.app.domain.DEFAULT_ACCOUNT_ICON
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.nextAccountIcon
import com.famly.app.domain.analytics.REPORT_PERIOD_LABELS
import com.famly.app.domain.analytics.ReportPeriod
import com.famly.app.domain.analytics.filterTransactionsByPeriod
import com.famly.app.domain.analytics.getReportPeriodDescription
import com.famly.app.domain.iconsForType
import com.famly.app.domain.recurring.RecurringProcessor
import com.famly.app.ui.components.CategoryListIcon
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.CategoryEmojiIcon
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.FamlyCategoryChip
import com.famly.app.ui.components.FamlyFilterChip
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.components.SectionHeading
import com.famly.app.ui.components.categoryAccentColor
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.TextSecondary
import com.famly.app.ui.theme.famlySmShadow
import com.famly.app.ui.theme.parseHexColor
import kotlinx.coroutines.launch

@Composable
fun OperationDetailScreen(
    state: FamlyUiState,
    transactionId: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSplit: () -> Unit,
    onUpdateRecurring: (Boolean, Int?) -> Unit,
) {
    val tx = state.transactions.find { it.id == transactionId } ?: return
    val cat = state.categories.find { it.id == tx.categoryId }
    val acc = state.accounts.find { it.id == tx.accountId }
    var recurring by remember(tx.id, tx.isRecurring) { mutableStateOf(tx.isRecurring) }
    var recurringDay by remember(tx.id, tx.recurringDay) {
        mutableStateOf(RecurringProcessor.effectiveRecurringDay(tx.recurringDay, tx.dateEpochDay))
    }

    ScreenScaffold(onBack = onBack) {
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
        FamlyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = recurring,
                    onCheckedChange = {
                        recurring = it
                        onUpdateRecurring(it, if (it) recurringDay else null)
                    },
                )
                Text("Повторять каждый месяц", fontWeight = FontWeight.SemiBold)
            }
            if (recurring) {
                Text("День месяца", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                        FamlyFilterChip(
                            label = "$day",
                            selected = recurringDay == day,
                            onClick = {
                                recurringDay = day
                                onUpdateRecurring(true, day)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        if (!tx.splitMemberIds.isNullOrBlank()) {
            DetailRow("Split", "Разделено с семьёй")
            TextButton(onClick = onSplit, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Изменить split →", color = Primary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (tx.type == "expense" && FamlyAccess.hasPremium(state.settings)) {
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
fun CategoryBudgetScreen(
    state: FamlyUiState,
    categoryId: String,
    onBack: () -> Unit,
    onSaveLimit: (Long) -> Unit,
    onToggleRollover: (Boolean) -> Unit,
) {
    val cat = state.categories.find { it.id == categoryId } ?: return
    var limit by remember(cat) { mutableStateOf(((cat.budgetLimitKopecks ?: 0) / 100).toString()) }
    val effectiveLimit = BudgetRolloverProcessor.effectiveLimit(cat)

    ScreenScaffold(onBack = onBack) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = Spacing.sm),
        ) {
            CategoryEmojiIcon(emoji = cat.icon, size = 40.dp, accent = categoryAccentColor(cat.color))
            Text(
                cat.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it; onSaveLimit(it.toLongOrNull() ?: 0) },
                label = { Text("Лимит бюджета (₽)") },
                modifier = Modifier.fillMaxWidth(),
            )
            if (cat.rolloverKopecks > 0) {
                Text(
                    "Перенос с прошлого периода: ${MoneyFormatter.formatKopecks(cat.rolloverKopecks)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Text(
                "Эффективный лимит: ${MoneyFormatter.formatKopecks(effectiveLimit)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = cat.rolloverEnabled,
                    onCheckedChange = onToggleRollover,
                )
                Text("Переносить неиспользованный остаток", modifier = Modifier.padding(start = 8.dp))
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
    syncStatus: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onSyncNow: () -> Unit,
    onOpenFamily: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var startDayInput by remember(state.settings.budgetPeriod.startDay) {
        mutableStateOf(state.settings.budgetPeriod.startDay.toString())
    }
    val context = LocalContext.current

    ScreenScaffold(onBack = onBack) {
        Row(
            modifier = Modifier.padding(bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.08f))
                    .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Text(
                "Семья и синхронизация",
                modifier = Modifier.padding(start = Spacing.sm),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg), padding = 14.dp) {
            Text(
                when {
                    state.settings.isSynced -> "Аккаунт подключён · семья «${state.settings.householdName ?: "без названия"}»"
                    state.settings.isAuthenticated -> "Аккаунт подключён · создайте семью на странице «Семья»"
                    else -> "Войдите, чтобы синхронизировать бюджет между устройствами"
                },
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            if (!state.settings.isAuthenticated) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Имя") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onLogin(email, password) }, modifier = Modifier.weight(1f)) { Text("Войти") }
                    Button(onClick = { onRegister(email, password, displayName) }, modifier = Modifier.weight(1f)) { Text("Регистрация") }
                }
            } else if (!state.settings.isSynced) {
                Text(
                    "Название семьи и приглашения — на странице «Семья» в разделе «Ещё».",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Button(onClick = onOpenFamily, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Перейти в «Семья»", modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                Button(onClick = onSyncNow, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Синхронизировать сейчас", modifier = Modifier.padding(start = 8.dp))
                }
            }
            syncStatus?.let {
                Text(it, fontSize = 12.sp, color = if (it.contains("Hostname") || it.contains("error", true) || it.contains("Ошибка")) Expense else Primary, modifier = Modifier.padding(top = 10.dp))
            }
        }
        SectionHeading("📊", "Бюджет")
        FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg), padding = 0.dp) {
            SettingRow(
                icon = "📅",
                label = "Начало периода",
                hint = "День месяца (1–28), с которого начинается бюджет",
            ) {
                OutlinedTextField(
                    value = startDayInput,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(2)
                        startDayInput = digits
                        digits.toIntOrNull()?.coerceIn(1, 28)?.let { onStartDayChange(it) }
                    },
                    modifier = Modifier.width(72.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                )
            }
            HorizontalDivider(color = Primary.copy(alpha = 0.12f), thickness = 1.dp)
            SettingRow(
                icon = "₽",
                label = "Валюта",
                hint = "Отображение сумм в приложении",
                isLast = true,
            ) {
                Text(
                    state.settings.currency,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(Primary.copy(alpha = 0.08f))
                        .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.sm))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("RUB", "USD", "EUR").forEach { code ->
                FamlyFilterChip(
                    label = code,
                    selected = state.settings.currency == code,
                    onClick = { onCurrencyChange(code) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SectionHeading("🎨", "Оформление")
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FamlyFilterChip(
                label = "☀️ Светлая",
                selected = state.settings.theme == "light",
                onClick = { onThemeChange("light") },
                modifier = Modifier.weight(1f),
            )
            FamlyFilterChip(
                label = "🌙 Тёмная",
                selected = state.settings.theme == "dark",
                onClick = { onThemeChange("dark") },
                modifier = Modifier.weight(1f),
            )
        }
        SectionHeading("📄", "Документы", modifier = Modifier.padding(top = Spacing.md))
        Column(modifier = Modifier.padding(start = 4.dp, bottom = Spacing.lg)) {
            listOf(
                "Политика конфиденциальности" to "${BuildConfig.API_BASE_URL}/legal/privacy",
                "Пользовательское соглашение" to "${BuildConfig.API_BASE_URL}/legal/terms",
            ).forEach { (label, url) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("•", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
                    Text(label, color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: String,
    label: String,
    hint: String? = null,
    isLast: Boolean = false,
    trailing: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(Primary.copy(alpha = 0.06f))
                    .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.sm)),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (hint != null) {
                    Text(hint, fontSize = 12.sp, color = TextMuted)
                }
            }
            trailing()
        }
        if (!isLast) {
            HorizontalDivider(color = Primary.copy(alpha = 0.12f), thickness = 1.dp)
        }
    }
}

@Composable
fun PremiumPaywallScreen(state: FamlyUiState, onBack: () -> Unit, onSubscribe: () -> Unit) {
    var plan by remember { mutableStateOf("yearly") }
    val trialDays = state.settings.trialDaysLeft()
    ScreenScaffold(onBack = onBack) {
        HeroCard(
            gradientStart = Premium,
            gradientEnd = Color(0xFFA67C00),
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 38.sp, modifier = Modifier.padding(end = 14.dp))
                Column {
                    Text("Подписка", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Text(
                        "Премиум — наш бюджет",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    if (trialDays > 0) {
                        Text(
                            "Пробный период: осталось $trialDays дн.",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FamlyFilterChip(
                label = "199 ₽/мес",
                selected = plan == "monthly",
                onClick = { plan = "monthly" },
                modifier = Modifier.weight(1f),
                accent = Premium,
            )
            FamlyFilterChip(
                label = "1500 ₽/год",
                selected = plan == "yearly",
                onClick = { plan = "yearly" },
                modifier = Modifier.weight(1f),
                accent = Premium,
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TierCard(
                "📋",
                "Бесплатно",
                listOf(
                    "Неограниченный ввод операций",
                    "Бюджет по категориям",
                    "Дневной лимит трат",
                    "Свой бюджетный период",
                    "Резервная копия JSON и CSV за 30 дней",
                ),
                premium = false,
                modifier = Modifier.weight(1f),
            )
            TierCard(
                "⭐",
                "Премиум",
                listOf(
                    "Семья до 6 человек",
                    "Облачная синхронизация",
                    "Роли и приватность",
                    "Делить расходы и учёт долгов",
                    "Перенос остатка бюджета",
                    "Расширенная аналитика",
                    "Выгрузка CSV без ограничений",
                    "Виджет быстрого ввода",
                ),
                premium = true,
                modifier = Modifier.weight(1f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.lg))
                .background(Premium)
                .clickable(onClick = onSubscribe)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 20.sp)
                Text(
                    if (trialDays > 0) "Подключить Премиум" else "Оформить подписку",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        Text(
            "Оплата через RuStore · Отмена в любой момент",
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = TextMuted,
        )
    }
}

@Composable
private fun TierCard(icon: String, title: String, features: List<String>, premium: Boolean, modifier: Modifier = Modifier) {
    FamlyCard(
        modifier = modifier,
        borderColor = if (premium) Premium.copy(alpha = 0.33f) else Primary.copy(alpha = 0.27f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Text(icon, fontSize = 20.sp)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
        }
        features.forEach {
            Text("✓ $it", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
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
    initialCategoryId: String? = null,
    initialType: String? = null,
    onDismiss: () -> Unit,
    onSave: (amount: String, type: String, categoryId: String, accountId: String, note: String, recurring: Boolean) -> Unit,
) {
    if (!visible) return
    var amount by remember { mutableStateOf("") }
    var type by remember(initialCategoryId, initialType) {
        mutableStateOf(initialType ?: if (initialCategoryId != null) "expense" else "expense")
    }
    var categoryId by remember(initialCategoryId, initialType, state.categories) {
        mutableStateOf(
            initialCategoryId
                ?: state.categories.firstOrNull { it.type == (initialType ?: "expense") }?.id
                ?: "",
        )
    }
    var accountId by remember { mutableStateOf(state.accounts.firstOrNull()?.id ?: "") }
    var note by remember { mutableStateOf("") }
    var recurring by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Primary)
                Text(
                    "Новая операция",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FamlyFilterChip(
                    label = "Расход",
                    selected = type == "expense",
                    onClick = {
                        type = "expense"
                        categoryId = state.categories.firstOrNull { it.type == "expense" }?.id ?: categoryId
                    },
                    modifier = Modifier.weight(1f),
                    accent = Expense,
                    leading = { Icon(Icons.Default.Remove, contentDescription = null, tint = if (type == "expense") Color.White else Expense, modifier = Modifier.size(18.dp)) },
                )
                FamlyFilterChip(
                    label = "Доход",
                    selected = type == "income",
                    onClick = {
                        type = "income"
                        categoryId = state.categories.firstOrNull { it.type == "income" }?.id ?: categoryId
                    },
                    modifier = Modifier.weight(1f),
                    accent = Income,
                    leading = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = if (type == "income") Color.White else Income, modifier = Modifier.size(18.dp)) },
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
            LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories.filter { it.type == type }) { cat ->
                    FamlyCategoryChip(
                        label = "${cat.icon} ${cat.name}",
                        selected = categoryId == cat.id,
                        onClick = { categoryId = cat.id },
                        accent = categoryAccentColor(cat.color),
                    )
                }
            }
            Text("Счёт", style = MaterialTheme.typography.labelMedium)
            LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.accounts) { acc ->
                    FamlyCategoryChip(
                        label = "${acc.icon} ${acc.name}",
                        selected = accountId == acc.id,
                        onClick = { accountId = acc.id },
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
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Сохранить", modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountsScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onCycleIcon: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var newIcon by remember { mutableStateOf(DEFAULT_ACCOUNT_ICON) }
    val total = state.accounts.sumOf { it.balanceKopecks }
    ScreenScaffold(onBack = onBack) {
        HeroCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(Radius.md))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(Radius.md)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💰", fontSize = 28.sp)
                }
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text("Общий баланс", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp)
                    Text(
                        MoneyFormatter.formatKopecks(total),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                }
            }
        }
        state.accounts.forEach { acc ->
            FamlyCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                cornerRadius = Radius.md,
                padding = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(Primary.copy(alpha = 0.06f))
                            .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.sm))
                            .clickable { onCycleIcon(acc.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(acc.icon, fontSize = 22.sp)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(acc.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(MoneyFormatter.formatKopecks(acc.balanceKopecks), color = TextMuted, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(Expense.copy(alpha = 0.06f))
                            .border(2.dp, Expense.copy(alpha = 0.25f), RoundedCornerShape(Radius.sm))
                            .clickable { onDelete(acc.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✕", color = Expense, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .famlySmShadow(RoundedCornerShape(Radius.md))
                    .clip(RoundedCornerShape(Radius.md))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.md))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    newIcon,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable { newIcon = nextAccountIcon(newIcon) }
                        .padding(end = 10.dp),
                )
                BasicTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(Primary),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (newName.isEmpty()) Text("Новый счёт...", color = TextMuted)
                        inner()
                    },
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(Primary)
                    .clickable {
                        if (newName.isNotBlank()) {
                            onAdd(newName.trim(), newIcon)
                            newName = ""
                            newIcon = DEFAULT_ACCOUNT_ICON
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
    val isPremium = FamlyAccess.hasPremium(state.settings)

    ScreenScaffold(onBack = onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                items(ReportPeriod.entries.toList()) { p ->
                    FamlyFilterChip(
                        label = REPORT_PERIOD_LABELS[p] ?: p.name,
                        selected = period == p,
                        onClick = { period = p },
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
    onAdd: (String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onCycleIcon: (String) -> Unit,
) {
    var tab by remember { mutableStateOf("expense") }
    var newName by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var selectedIcon by remember(tab) { mutableStateOf(if (tab == "expense") DEFAULT_EXPENSE_ICON else DEFAULT_INCOME_ICON) }
    var selectedColor by remember(tab) { mutableStateOf(if (tab == "expense") "#457B9D" else "#2D6A4F") }

    ScreenScaffold(onBack = onBack) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FamlyFilterChip(
                label = "Расходы",
                selected = tab == "expense",
                onClick = {
                    tab = "expense"
                    selectedIcon = DEFAULT_EXPENSE_ICON
                    selectedColor = "#457B9D"
                },
                accent = Expense,
                modifier = Modifier.weight(1f),
                leading = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (tab == "expense") Color.White else TextSecondary,
                    )
                },
            )
            FamlyFilterChip(
                label = "Доходы",
                selected = tab == "income",
                onClick = {
                    tab = "income"
                    selectedIcon = DEFAULT_INCOME_ICON
                    selectedColor = "#2D6A4F"
                },
                accent = Income,
                modifier = Modifier.weight(1f),
                leading = {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (tab == "income") Color.White else TextSecondary,
                    )
                },
            )
        }
        state.categories.filter { it.type == tab }.forEach { cat ->
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryEmojiIcon(
                        emoji = cat.icon,
                        size = 40.dp,
                        accent = parseHexColor(cat.color),
                        modifier = Modifier
                            .clickable { onCycleIcon(cat.id) }
                            .padding(end = 12.dp),
                    )
                    Text(cat.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(Expense.copy(alpha = 0.08f))
                            .border(2.dp, Expense.copy(alpha = 0.25f), RoundedCornerShape(Radius.sm))
                            .clickable { onDelete(cat.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✕", color = Expense, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (!showForm) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .border(2.dp, Primary, RoundedCornerShape(Radius.md))
                    .clickable { showForm = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("+ Добавить категорию", color = Primary, fontWeight = FontWeight.SemiBold)
            }
        } else {
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Primary.copy(alpha = 0.08f))
                            .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryListIcon(Modifier.size(18.dp))
                    }
                    Text("Новая категория", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.md))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(end = 10.dp))
                    BasicTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(Primary),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (newName.isEmpty()) Text("Название...", color = TextMuted)
                            inner()
                        },
                    )
                }
                Text(
                    "Выберите иконку",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
                    color = TextMuted,
                )
                iconsForType(tab).chunked(5).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowIcons.forEach { iconDef ->
                            val selected = selectedIcon == iconDef.emoji
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .background(if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(
                                        2.dp,
                                        if (selected) Primary else Primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(Radius.sm),
                                    )
                                    .clickable {
                                        selectedIcon = iconDef.emoji
                                        selectedColor = iconDef.color
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(iconDef.emoji, fontSize = 22.sp)
                            }
                        }
                        repeat(5 - rowIcons.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.md))
                            .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.md))
                            .clickable {
                                showForm = false
                                newName = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Text("Отмена", fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Primary)
                            .clickable {
                                if (newName.isNotBlank()) {
                                    onAdd(newName.trim(), tab, selectedIcon, selectedColor)
                                    newName = ""
                                    showForm = false
                                    selectedIcon = if (tab == "expense") DEFAULT_EXPENSE_ICON else DEFAULT_INCOME_ICON
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(start = 6.dp).size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
