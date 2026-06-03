package com.famly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.analytics.getCategorySpent
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.BudgetProgressBar
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.TrialBanner
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing

@Composable
fun HomeScreen(
    state: FamlyUiState,
    onOpenBudget: () -> Unit,
    onOpenOperations: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenPremium: () -> Unit,
) {
    var visibleRecent by remember { mutableIntStateOf(5) }
    val recent = state.transactions.take(visibleRecent)

    Column(modifier = Modifier.padding(Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Мой (Наш) Бюджет",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Text("Привет! 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            val trialDays = state.settings.trialDaysLeft()
            when {
                state.settings.isPremium -> PremiumBadge("Премиум")
                trialDays > 0 -> PremiumBadge("Trial: $trialDays дн.")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        HeroCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenBudget),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.periodLabel,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Бюджет →", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text("В день можно тратить", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
            Text(
                MoneyFormatter.formatKopecks(state.dailySafeSpendKopecks),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Остаток: ${MoneyFormatter.formatKopecks(state.safeToSpendKopecks)}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("До конца: ${state.daysLeft} дн.", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            BudgetProgressBar(spent = state.spentKopecks, limit = state.budgetTotalKopecks, color = Color.White)
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Доходы", MoneyFormatter.formatKopecks(state.incomeKopecks), Income, Modifier.weight(1f))
            StatCard("Расходы", MoneyFormatter.formatKopecks(state.spentKopecks), Expense, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Последние операции", fontWeight = FontWeight.SemiBold)
            Text("Все →", color = Primary, modifier = Modifier.clickable(onClick = onOpenOperations))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        recent.forEach { tx ->
            TransactionRow(state, tx, onOpenTransaction)
        }
        if (visibleRecent < state.transactions.size) {
            Text(
                "Показать ещё",
                color = Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { visibleRecent += 10 }
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun PremiumBadge(text: String) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(com.famly.app.ui.theme.PremiumBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = Premium,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    FamlyCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun OperationsScreen(state: FamlyUiState, onOpenTransaction: (String) -> Unit) {
    var search by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("all") }

    val filtered = state.transactions.filter { tx ->
        if (filterType != "all" && tx.type != filterType) return@filter false
        if (search.isBlank()) return@filter true
        val cat = state.categories.find { it.id == tx.categoryId }
        val q = search.lowercase()
        cat?.name?.lowercase()?.contains(q) == true ||
            tx.note?.lowercase()?.contains(q) == true ||
            tx.amountKopecks.toString().contains(q)
    }

    LazyColumn(modifier = Modifier.padding(horizontal = Spacing.md)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(Radius.md))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔍", modifier = Modifier.padding(end = 8.dp))
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { inner ->
                        if (search.isEmpty()) {
                            Text("Поиск...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                        }
                        inner()
                    },
                )
            }
            Row(modifier = Modifier.padding(bottom = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "Все", "expense" to "Расходы", "income" to "Доходы").forEach { (id, label) ->
                    FilterChip(
                        selected = filterType == id,
                        onClick = { filterType = id },
                        label = { Text(label) },
                    )
                }
            }
        }
        items(filtered, key = { it.id }) { tx ->
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
                    .clickable { onOpenTransaction(tx.id) },
            ) {
                TransactionRow(state, tx, onOpenTransaction, compact = false)
            }
        }
    }
}

@Composable
private fun TransactionRow(
    state: FamlyUiState,
    tx: com.famly.app.data.local.entity.TransactionEntity,
    onOpen: (String) -> Unit,
    compact: Boolean = true,
) {
    val cat = state.categories.find { it.id == tx.categoryId }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (compact) Modifier.clickable { onOpen(tx.id) }.padding(vertical = 10.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(cat?.icon ?: "📝", fontSize = if (compact) 20.sp else 24.sp, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cat?.name ?: "—", fontWeight = FontWeight.Medium)
            Text(
                buildString {
                    append(MoneyFormatter.formatShortDate(tx.dateEpochDay))
                    if (tx.isRecurring) append(" · 🔄")
                    if (!tx.splitMemberIds.isNullOrBlank()) append(" · 👪")
                    if (!compact && !tx.note.isNullOrBlank()) append(" · ${tx.note}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Text(
            "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
            fontWeight = FontWeight.SemiBold,
            color = if (tx.type == "expense") Expense else Income,
        )
    }
}

@Composable
fun BudgetScreen(
    state: FamlyUiState,
    onOpenCategory: (String) -> Unit,
    onOpenCategories: () -> Unit,
) {
    Column(modifier = Modifier.padding(Spacing.md)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Бюджет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Категории", color = Primary, modifier = Modifier.clickable(onClick = onOpenCategories))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        HeroCard(modifier = Modifier.fillMaxWidth()) {
            Text(state.periodLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            Text(
                MoneyFormatter.formatKopecks(state.safeToSpendKopecks),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text("можно потратить", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(Spacing.sm))
            BudgetProgressBar(spent = state.spentKopecks, limit = state.budgetTotalKopecks, color = Color.White)
            Text(
                "${MoneyFormatter.formatKopecks(state.spentKopecks)} / ${MoneyFormatter.formatKopecks(state.budgetTotalKopecks)}",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        state.categories.filter { it.type == "expense" && it.budgetLimitKopecks != null }.forEach { cat ->
            val spent = getCategorySpent(cat.id, state.transactions)
            val limit = cat.budgetLimitKopecks ?: 0L
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
                    .clickable { onOpenCategory(cat.id) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cat.icon, modifier = Modifier.padding(end = 8.dp))
                    Text(cat.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                BudgetProgressBar(spent = spent, limit = limit)
                Text(
                    "${MoneyFormatter.formatKopecks(spent)} / ${MoneyFormatter.formatKopecks(limit)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun MoreScreen(
    state: FamlyUiState,
    onNavigate: (String) -> Unit,
    onOpenPremium: () -> Unit,
) {
    val items = listOf(
        Triple("💳", "Счета", "accounts"),
        Triple("📈", "Отчёты", "reports"),
        Triple("⚙️", "Настройки", "settings"),
        Triple("💾", "Backup и экспорт", "backup"),
        Triple("👨‍👩‍👧", "Семья", "family"),
        Triple("⚖️", "Балансы IOU", "balances"),
        Triple("📉", "Аналитика", "analytics"),
        Triple("⭐", "Премиум", "premium"),
    )
    Column(modifier = Modifier.padding(Spacing.md)) {
        TrialBanner(
            trialDaysLeft = state.settings.trialDaysLeft(),
            isPremium = state.settings.isPremium,
            onUpgrade = onOpenPremium,
        )
        items.forEach { (icon, label, route) ->
            val premiumOnly = route in listOf("family", "balances", "analytics")
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
                    .clickable { onNavigate(route) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Text(icon, fontSize = 24.sp)
                    }
                    Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    if (premiumOnly) {
                        Text("Премиум", color = Premium, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("›", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 18.sp)
                }
            }
        }
        Text(
            "Мой (Наш) Бюджет v0.1.0 · Сделано в России",
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        )
    }
}
