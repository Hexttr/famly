package com.famly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.famly.app.data.local.entity.CategoryEntity
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.analytics.getCategorySpent
import com.famly.app.domain.analytics.getTopExpenseCategoryIds
import com.famly.app.domain.FamlyAccess
import com.famly.app.domain.budget.BudgetRolloverProcessor
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.AccentCard
import com.famly.app.ui.components.AccentCardColumn
import com.famly.app.ui.components.BudgetProgressBar
import com.famly.app.ui.components.CategoryEmojiIcon
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.FamlyFilterChip
import com.famly.app.ui.components.FamlySearchBar
import com.famly.app.ui.components.GroupedListCard
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.QuickCategoryTile
import com.famly.app.ui.components.TrialBanner
import com.famly.app.ui.components.categoryAccentColor
import com.famly.app.ui.theme.Border
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.HeroHint
import com.famly.app.ui.theme.Income
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.LayoutInsets
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.TextSecondary

private const val INITIAL_RECENT = 5
private const val LOAD_MORE_STEP = 10

@Composable
fun HomeScreen(
    state: FamlyUiState,
    onOpenBudget: () -> Unit,
    onOpenOperations: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onQuickAddCategory: (String) -> Unit,
) {
    var visibleRecent by remember { mutableIntStateOf(INITIAL_RECENT) }
    val recent = state.transactions.take(visibleRecent)
    val hasMore = visibleRecent < state.transactions.size
    val remaining = maxOf(0, state.safeToSpendKopecks)
    val net = state.incomeKopecks - state.spentKopecks
    val budgetConfigured = state.budgetTotalKopecks > 0

    val topIds = getTopExpenseCategoryIds(state.transactions)
    val quickFromTop = topIds.mapNotNull { id -> state.categories.find { it.id == id } }.take(4)
    val fallbackQuick = state.categories.filter { it.type == "expense" }.take(4)
    val quickCategories = if (quickFromTop.size >= 4) quickFromTop else fallbackQuick

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = LayoutInsets.mainTabScrollBottom),
    ) {
        HeroCard(
            modifier = Modifier
                .padding(horizontal = Spacing.md)
                .padding(bottom = 10.dp),
            onClick = onOpenBudget,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        state.periodLabel,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.14f), androidx.compose.foundation.shape.RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Бюджет", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (budgetConfigured) {
                    Text("Можно тратить", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        MoneyFormatter.formatKopecks(remaining),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 42.sp,
                    )
                    Text(
                        "около ${MoneyFormatter.formatKopecks(state.dailySafeSpendKopecks)} / день · ${state.daysLeft} дн.",
                        color = HeroHint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 14.dp, top = 4.dp),
                    )
                    BudgetProgressBar(
                        spent = state.spentKopecks,
                        limit = state.budgetTotalKopecks,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f),
                        height = 6.dp,
                        showLabel = true,
                        label = "Потрачено ${MoneyFormatter.formatKopecks(state.spentKopecks)} из ${MoneyFormatter.formatKopecks(state.budgetTotalKopecks)}",
                        labelColor = Color.White.copy(alpha = 0.85f),
                    )
                } else {
                    Text(
                        if (state.spentKopecks > 0) "Потрачено за период" else "Начните с учёта",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (state.spentKopecks > 0) {
                        Text(
                            MoneyFormatter.formatKopecks(state.spentKopecks),
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 42.sp,
                        )
                    } else {
                        Text(
                            "Добавьте операцию\nили настройте бюджет",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        )
                    }
                    Text(
                        "Настроить бюджет →",
                        color = HeroHint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                    )
                }
            }
        }

        if (quickCategories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.md)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                quickCategories.forEach { cat ->
                    QuickCategoryTile(
                        emoji = cat.icon,
                        name = cat.name,
                        accent = categoryAccentColor(cat.color),
                        onClick = { onQuickAddCategory(cat.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (budgetConfigured) {
            AccentCard(
                modifier = Modifier
                    .padding(horizontal = Spacing.md)
                    .padding(bottom = 12.dp),
            ) {
                Text("Текущая экономия", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${if (net >= 0) "+" else "−"}${MoneyFormatter.formatKopecks(kotlin.math.abs(net))}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) Income else Expense,
                    )
                    Text(
                        "${MoneyFormatter.formatKopecks(state.incomeKopecks)} − ${MoneyFormatter.formatKopecks(state.spentKopecks)}",
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }
        }

        GroupedListCard(modifier = Modifier.padding(horizontal = Spacing.md)) {
            recent.forEachIndexed { index, tx ->
                val isLast = index == recent.lastIndex && !hasMore
                HomeTransactionRow(state, tx, onOpenTransaction)
                if (!isLast) {
                    HorizontalDivider(color = Border, thickness = 1.dp)
                }
            }
        }

        if (hasMore) {
            AccentCardColumn(
                modifier = Modifier
                    .padding(horizontal = Spacing.md)
                    .padding(top = 12.dp),
                onClick = { visibleRecent = minOf(visibleRecent + LOAD_MORE_STEP, state.transactions.size) },
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text(
                        "Показать ещё",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTransactionRow(
    state: FamlyUiState,
    tx: TransactionEntity,
    onOpen: (String) -> Unit,
) {
    val cat = state.categories.find { it.id == tx.categoryId }
    val subtitle = if (!tx.note.isNullOrBlank()) {
        "${tx.note} · ${MoneyFormatter.formatShortDate(tx.dateEpochDay)}"
    } else {
        MoneyFormatter.formatShortDate(tx.dateEpochDay)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(tx.id) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (cat != null) {
            CategoryEmojiIcon(emoji = cat.icon, size = 36.dp, accent = categoryAccentColor(cat.color))
        } else {
            Text("📝", fontSize = 20.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(cat?.name ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (tx.type == "expense") Expense else Income,
        )
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

    LazyColumn(
        modifier = Modifier.padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = LayoutInsets.mainTabScrollBottom),
    ) {
        item {
            FamlySearchBar(
                value = search,
                onValueChange = { search = it },
                placeholder = "Поиск...",
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FamlyFilterChip(
                    label = "Все",
                    selected = filterType == "all",
                    onClick = { filterType = "all" },
                    modifier = Modifier.weight(1f),
                    accent = Primary,
                    leading = { Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(16.dp), tint = if (filterType == "all") Color.White else TextSecondary) },
                )
                FamlyFilterChip(
                    label = "Расходы",
                    selected = filterType == "expense",
                    onClick = { filterType = "expense" },
                    modifier = Modifier.weight(1f),
                    accent = Expense,
                    leading = { Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp), tint = if (filterType == "expense") Color.White else TextSecondary) },
                )
                FamlyFilterChip(
                    label = "Доходы",
                    selected = filterType == "income",
                    onClick = { filterType = "income" },
                    modifier = Modifier.weight(1f),
                    accent = Income,
                    leading = { Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp), tint = if (filterType == "income") Color.White else TextSecondary) },
                )
            }
        }
        items(filtered, key = { it.id }) { tx ->
            OperationTransactionCard(state, tx, onOpenTransaction)
        }
        item { Spacer(modifier = Modifier.height(Spacing.sm)) }
    }
}

@Composable
private fun OperationTransactionCard(
    state: FamlyUiState,
    tx: TransactionEntity,
    onOpen: (String) -> Unit,
) {
    val cat = state.categories.find { it.id == tx.categoryId }
    FamlyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(tx.id) },
        cornerRadius = Radius.md,
        padding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cat != null) {
                CategoryEmojiIcon(emoji = cat.icon, size = 38.dp, accent = categoryAccentColor(cat.color))
            } else {
                Text("📝", fontSize = 22.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(cat?.name ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(MoneyFormatter.formatShortDate(tx.dateEpochDay))
                        if (!tx.note.isNullOrBlank()) append(" · ${tx.note}")
                        if (tx.isRecurring) append(" · 🔄")
                    },
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "${if (tx.type == "expense") "−" else "+"}${MoneyFormatter.formatKopecks(tx.amountKopecks)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "expense") Expense else Income,
            )
        }
    }
}

@Composable
fun BudgetScreen(
    state: FamlyUiState,
    onOpenCategory: (String) -> Unit,
    onOpenCategories: () -> Unit,
    onReorderCategories: (List<String>) -> Unit = {},
) {
    val budgetCategories = state.categories.filter { it.type == "expense" && it.budgetLimitKopecks != null }
    var orderedCategories by remember(budgetCategories.map { it.id }) {
        mutableStateOf(budgetCategories)
    }
    LaunchedEffect(budgetCategories.map { it.id }) {
        val newIds = budgetCategories.map { it.id }
        val currentIds = orderedCategories.map { it.id }
        orderedCategories = if (currentIds == newIds) {
            orderedCategories.mapNotNull { old -> budgetCategories.find { it.id == old.id } }
        } else {
            budgetCategories
        }
    }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val headerCount = 2
        val fromIndex = from.index - headerCount
        val toIndex = to.index - headerCount
        if (fromIndex in orderedCategories.indices && toIndex in orderedCategories.indices) {
            orderedCategories = orderedCategories.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            onReorderCategories(orderedCategories.map { it.id })
        }
    }
    val pct = if (state.budgetTotalKopecks > 0) {
        (state.spentKopecks * 100 / state.budgetTotalKopecks).toInt()
    } else {
        0
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
        contentPadding = PaddingValues(bottom = LayoutInsets.mainTabScrollBottom),
    ) {
        item {
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            state.periodLabel,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.14f), androidx.compose.foundation.shape.RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onOpenCategories),
                        ) {
                            Text("Категории", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Общий бюджет периода", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        MoneyFormatter.formatKopecks(state.budgetTotalKopecks),
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BudgetProgressBar(
                        spent = state.spentKopecks,
                        limit = state.budgetTotalKopecks,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f),
                        height = 6.dp,
                        showLabel = true,
                        label = "Потрачено ${MoneyFormatter.formatKopecks(state.spentKopecks)} из ${MoneyFormatter.formatKopecks(state.budgetTotalKopecks)} · $pct%",
                        labelColor = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(Spacing.md)) }
        items(orderedCategories, key = { it.id }) { cat ->
            ReorderableItem(reorderableLazyListState, key = cat.id) { isDragging ->
                BudgetCategoryCard(
                    cat = cat,
                    transactions = state.transactions,
                    isDragging = isDragging,
                    onOpenCategory = onOpenCategory,
                    dragHandleModifier = Modifier.draggableHandle(),
                )
            }
        }
    }
}

@Composable
private fun BudgetCategoryCard(
    cat: CategoryEntity,
    transactions: List<TransactionEntity>,
    isDragging: Boolean,
    onOpenCategory: (String) -> Unit,
    dragHandleModifier: Modifier,
) {
    val spent = getCategorySpent(cat.id, transactions)
    val limit = BudgetRolloverProcessor.effectiveLimit(cat)
    val catPct = if (limit > 0) (spent * 100 / limit).toInt() else 0
    FamlyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .graphicsLayer {
                alpha = if (isDragging) 0.92f else 1f
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
            }
            .clickable { onOpenCategory(cat.id) },
        cornerRadius = Radius.md,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Box(
                modifier = dragHandleModifier.padding(end = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Перетащить",
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp),
                )
            }
            CategoryEmojiIcon(emoji = cat.icon, size = 36.dp, accent = categoryAccentColor(cat.color))
            Text(cat.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f).padding(start = 10.dp))
            Text(
                "$catPct%",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (catPct >= 100) Expense else TextSecondary,
            )
        }
        BudgetProgressBar(spent = spent, limit = limit, color = categoryAccentColor(cat.color), height = 6.dp)
        Text(
            "${MoneyFormatter.formatKopecks(spent)} / ${MoneyFormatter.formatKopecks(limit)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
fun MoreScreen(
    state: FamlyUiState,
    onNavigate: (String) -> Unit,
    onOpenPremium: () -> Unit,
) {
    val showMonetization = FamlyAccess.showPaywall()
    val items = buildList {
        add(MoreMenuItem(Icons.Default.Autorenew, "Периодические", "recurring"))
        add(MoreMenuItem(Icons.Default.CreditCard, "Счета", "accounts"))
        add(MoreMenuItem(Icons.AutoMirrored.Filled.ShowChart, "Отчёты", "reports"))
        add(MoreMenuItem(Icons.Default.Settings, "Настройки", "settings"))
        add(MoreMenuItem(Icons.Default.Save, "Backup и экспорт", "backup"))
        add(MoreMenuItem(Icons.Default.Group, "Семья", "family"))
        add(MoreMenuItem(Icons.Default.BarChart, "Аналитика", "analytics"))
        if (showMonetization) add(MoreMenuItem(Icons.Default.Star, "Premium", "premium"))
    }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .padding(bottom = LayoutInsets.mainTabScrollBottom),
    ) {
        if (showMonetization) {
            TrialBanner(
                trialDaysLeft = state.settings.trialDaysLeft(),
                isPremium = state.settings.isPremium,
                onUpgrade = onOpenPremium,
            )
        }
        items.forEach { item ->
            val premiumOnly = showMonetization && item.route in listOf("family", "analytics")
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onNavigate(item.route) },
                cornerRadius = Radius.md,
                padding = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                    Text(item.label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (premiumOnly) {
                        Text("Premium", color = Premium, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("›", color = TextMuted, fontSize = 18.sp)
                }
            }
        }
        Text(
            "Мой (Наш) Бюджет v1.0.4 · Сделано в России",
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.md, bottom = Spacing.md),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

private data class MoreMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
)
