package com.famly.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.R
import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.domain.MoneyFormatter
import com.famly.app.domain.savings.SavingsGoalProcessor
import com.famly.app.domain.savings.SavingsGoalType
import com.famly.app.domain.savings.savingsGoalDisplayName
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.theme.FamlyColor
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.TextMuted

@Composable
fun SavingsPreviewCard(
    goal: SavingsGoalEntity?,
    onOpen: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FamlyCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        cornerRadius = Radius.lg,
        padding = 0.dp,
    ) {
        if (goal != null && goal.isActive) {
            HeroCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpen,
            ) {
                SavingsHeroContent(
                    goalType = goal.goalType,
                    goalName = savingsGoalDisplayName(goal.goalType, goal.customName),
                    savedKopecks = goal.savedKopecks,
                    targetKopecks = goal.targetKopecks,
                    monthlyPlanKopecks = goal.monthlyPlanKopecks,
                    compact = true,
                    animateProgress = true,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreate)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FamlyColor.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = FamlyColor.primary)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("Копим", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Начните копить на мечту",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text("›", color = TextMuted, fontSize = 18.sp)
                }
                Button(
                    onClick = onCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(containerColor = FamlyColor.primary),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Создать цель", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SavingsHeroContent(
    goalType: String,
    goalName: String,
    savedKopecks: Long,
    targetKopecks: Long,
    monthlyPlanKopecks: Long?,
    compact: Boolean,
    animateProgress: Boolean,
) {
    if (compact) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoalIllustration(goalType, size = 88.dp)
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    "Копи на",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    goalName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                SavingsGoalProgressBar(
                    savedKopecks = savedKopecks,
                    targetKopecks = targetKopecks,
                    monthlyPlanKopecks = monthlyPlanKopecks,
                    animate = animateProgress,
                    modifier = Modifier.padding(top = 10.dp),
                    lightOnDark = true,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoalIllustration(goalType, size = 120.dp)
            Text(
                "Копи на",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                goalName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    "${SavingsGoalProcessor.progressPercent(savedKopecks, targetKopecks)}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            SavingsGoalProgressBar(
                savedKopecks = savedKopecks,
                targetKopecks = targetKopecks,
                monthlyPlanKopecks = monthlyPlanKopecks,
                animate = animateProgress,
                modifier = Modifier.padding(top = 14.dp),
                lightOnDark = true,
            )
        }
    }
}

@Composable
fun SavingsGoalProgressBar(
    savedKopecks: Long,
    targetKopecks: Long,
    monthlyPlanKopecks: Long? = null,
    animate: Boolean = false,
    modifier: Modifier = Modifier,
    lightOnDark: Boolean = false,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(animate) { if (animate) started = true }
    val targetFraction = if (targetKopecks <= 0) 0f else {
        (savedKopecks.toFloat() / targetKopecks).coerceIn(0f, 1f)
    }
    val fraction by animateFloatAsState(
        targetValue = if (started || !animate) targetFraction else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "savingsProgress",
    )
    val pct = SavingsGoalProcessor.progressPercent(savedKopecks, targetKopecks)
    val mutedColor = if (lightOnDark) Color.White.copy(alpha = 0.75f) else TextMuted
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(if (lightOnDark) Color.White.copy(alpha = 0.22f) else FamlyColor.primary.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (lightOnDark) Color.White.copy(alpha = 0.85f) else FamlyColor.primaryLight,
                                if (lightOnDark) Color.White else FamlyColor.primary,
                            ),
                        ),
                    ),
            )
        }
        Text(
            "${MoneyFormatter.formatKopecks(savedKopecks)} из ${MoneyFormatter.formatKopecks(targetKopecks)} · $pct%",
            color = mutedColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
        monthlyPlanKopecks?.takeIf { it > 0 }?.let { plan ->
            Text(
                "≈ ${MoneyFormatter.formatKopecks(plan)} / мес",
                color = mutedColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
fun GoalIllustration(goalType: String, size: androidx.compose.ui.unit.Dp = 72.dp) {
    val type = SavingsGoalType.fromKey(goalType)
    val drawable = when (type) {
        SavingsGoalType.CAR -> R.drawable.goal_car
        SavingsGoalType.APARTMENT -> R.drawable.goal_apartment
        SavingsGoalType.HOUSE -> R.drawable.goal_house
        SavingsGoalType.BUSINESS -> R.drawable.goal_business
        SavingsGoalType.VACATION -> R.drawable.goal_vacation
        SavingsGoalType.OTHER -> R.drawable.goal_other
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = type.label,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun SavingsSectionTitle(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = FamlyColor.primary, modifier = Modifier.size(20.dp))
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun SavingsScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onManualAdd: (String) -> Unit,
    onPause: () -> Unit,
) {
    val goal = state.savingsGoal
    var manualAmount by remember { mutableStateOf("") }
    var showManual by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            icon = { Icon(Icons.Default.Pause, contentDescription = null, tint = Expense) },
            title = { Text("Приостановить цель?") },
            text = {
                Text(
                    "Накопленная сумма вернётся на счета, с которых она была отложена. Цель можно будет создать заново.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        onPause()
                    },
                ) {
                    Text("Приостановить", color = Expense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    ScreenScaffold(onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = Spacing.sm)) {
            Icon(Icons.Default.Savings, contentDescription = null, tint = FamlyColor.primary)
            Text(
                "Копим",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (goal == null || !goal.isActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Savings, contentDescription = null, tint = FamlyColor.primary, modifier = Modifier.size(56.dp))
                Text("Цель не задана", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                Text("Создайте цель накопления", color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(containerColor = FamlyColor.primary),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Выбрать цель", modifier = Modifier.padding(start = 8.dp))
                }
            }
        } else {
            val name = savingsGoalDisplayName(goal.goalType, goal.customName)
            val monthlySaved = SavingsGoalProcessor.monthlyContributionsKopecks(
                state.savingsLedger,
                state.periodStartEpochDay,
                state.periodEndEpochDay,
            )
            val plan = goal.monthlyPlanKopecks ?: 0L
            val planRemaining = (plan - monthlySaved).coerceAtLeast(0)

            HeroCard(modifier = Modifier.fillMaxWidth()) {
                SavingsHeroContent(
                    goalType = goal.goalType,
                    goalName = name,
                    savedKopecks = goal.savedKopecks,
                    targetKopecks = goal.targetKopecks,
                    monthlyPlanKopecks = goal.monthlyPlanKopecks,
                    compact = false,
                    animateProgress = true,
                )
            }

            FamlyCard(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                padding = 0.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SavingsSectionTitle(icon = Icons.Default.CalendarMonth, title = "В этом месяце")
                    HorizontalDivider(color = FamlyColor.primary.copy(alpha = 0.12f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MonthlyStatColumn("Отложено", MoneyFormatter.formatKopecks(monthlySaved), FamlyColor.income)
                        if (plan > 0) {
                            MonthlyStatColumn("План", MoneyFormatter.formatKopecks(plan), FamlyColor.primary)
                            MonthlyStatColumn("Осталось", MoneyFormatter.formatKopecks(planRemaining), TextMuted)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Изменить", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = { showManual = !showManual }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Пополнить", modifier = Modifier.padding(start = 6.dp))
                }
            }
            if (showManual) {
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = manualAmount,
                        onValueChange = { manualAmount = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                        label = { Text("Сумма, ₽") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            onManualAdd(manualAmount)
                            manualAmount = ""
                            showManual = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FamlyColor.primary),
                    ) {
                        Text("Добавить в копилку")
                    }
                }
            }

            SavingsSectionTitle(
                icon = Icons.Default.History,
                title = "Последние движения",
                modifier = Modifier.padding(top = Spacing.md),
            )
            state.savingsLedger.take(10).forEach { entry ->
                SavingsLedgerRow(entry, name)
            }
            if (state.savingsLedger.isEmpty()) {
                Text("Пока нет записей", color = TextMuted, fontSize = 13.sp)
            }

            Button(
                onClick = { showPauseDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                shape = RoundedCornerShape(Radius.lg),
                colors = ButtonDefaults.buttonColors(containerColor = Expense),
            ) {
                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Приостановить цель", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun MonthlyStatColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SavingsLedgerRow(entry: SavingsLedgerEntity, goalName: String) {
    val (label, sign, icon) = when (entry.entryType) {
        "income_auto" -> Triple("Авто-отложение", "+", Icons.Default.TrendingUp)
        "manual_add" -> Triple("Пополнение", "+", Icons.Default.Add)
        "spend_from_goal" -> Triple("Списание с цели", "−", Icons.Default.Savings)
        "pause_release" -> Triple("Возврат при паузе", "−", Icons.Default.Pause)
        else -> Triple("Корректировка", if (entry.amountKopecks >= 0) "+" else "−", Icons.Default.Savings)
    }
    FamlyCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        padding = 12.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FamlyColor.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = FamlyColor.primary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(goalName, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                "$sign${MoneyFormatter.formatKopecks(kotlin.math.abs(entry.amountKopecks))}",
                fontWeight = FontWeight.Bold,
                color = if (entry.amountKopecks >= 0) FamlyColor.income else Expense,
            )
        }
    }
}

@Composable
fun SavingsSetupScreen(
    existing: SavingsGoalEntity?,
    onBack: () -> Unit,
    onSave: (
        goalType: String,
        customName: String?,
        targetRubles: String,
        incomePercent: Int?,
        monthlyPlanRubles: String?,
    ) -> Unit,
) {
    var selectedType by remember(existing) {
        mutableStateOf(SavingsGoalType.fromKey(existing?.goalType ?: SavingsGoalType.CAR.key))
    }
    var customName by remember(existing) { mutableStateOf(existing?.customName.orEmpty()) }
    var targetAmount by remember(existing) {
        mutableStateOf(existing?.targetKopecks?.let { (it / 100).toString() }.orEmpty())
    }
    var incomePercent by remember(existing) { mutableIntStateOf(existing?.incomePercent ?: 10) }
    var monthlyPlan by remember(existing) {
        mutableStateOf(existing?.monthlyPlanKopecks?.let { if (it > 0) (it / 100).toString() else "" }.orEmpty())
    }
    val previewIncome = 50_000L
    val previewSave = SavingsGoalProcessor.incomeAllocationKopecks(previewIncome * 100, incomePercent)
    val isEdit = existing?.isActive == true

    ScreenScaffold(onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = Spacing.sm)) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = FamlyColor.primary)
            Text(
                if (isEdit) "Изменить цель" else "Новая цель",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Text("Тип цели", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SavingsGoalType.entries.chunked(3).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowTypes.forEach { type ->
                        val selected = selectedType == type
                        FamlyCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                                .then(
                                    if (selected) {
                                        Modifier.border(2.dp, FamlyColor.primary, RoundedCornerShape(Radius.md))
                                    } else {
                                        Modifier
                                    },
                                ),
                            padding = 8.dp,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                GoalIllustration(type.key, size = 56.dp)
                                Text(
                                    type.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                    repeat(3 - rowTypes.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (selectedType == SavingsGoalType.OTHER) {
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = { Text("Название цели") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                singleLine = true,
            )
        }

        FamlyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .border(2.dp, FamlyColor.primary.copy(alpha = 0.35f), RoundedCornerShape(Radius.lg)),
            padding = 16.dp,
            cornerRadius = Radius.lg,
        ) {
            Text("Сумма цели", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                placeholder = { Text("0") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = FamlyColor.primary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("₽", fontWeight = FontWeight.Bold, color = FamlyColor.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                ),
            )
        }

        Text(
            "% с каждого дохода: $incomePercent%",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Slider(
            value = incomePercent.toFloat(),
            onValueChange = { incomePercent = (it / 5).toInt() * 5 },
            valueRange = 0f..50f,
            steps = 9,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "При доходе ${MoneyFormatter.formatKopecks(previewIncome * 100)} → ${MoneyFormatter.formatKopecks(previewSave)} на цель",
            color = TextMuted,
            fontSize = 13.sp,
        )
        OutlinedTextField(
            value = monthlyPlan,
            onValueChange = { monthlyPlan = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
            label = { Text("План в месяц (необязательно), ₽") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        Button(
            onClick = {
                onSave(
                    selectedType.key,
                    customName.takeIf { selectedType == SavingsGoalType.OTHER },
                    targetAmount,
                    incomePercent.takeIf { it > 0 },
                    monthlyPlan,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            shape = RoundedCornerShape(Radius.lg),
            colors = ButtonDefaults.buttonColors(containerColor = FamlyColor.primary),
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                if (isEdit) "Сохранить" else "Начать копить",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
