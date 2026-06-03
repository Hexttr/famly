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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.FamlyFilterChip
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.famlySmShadow

@Composable
fun FamilyScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenMember: (String) -> Unit,
) {
    if (!state.settings.hasPremiumAccess()) {
        ScreenScaffold(onBack = onBack) {
            PremiumGateScreen("Семейный бюджет", onUpgrade)
        }
        return
    }
    ScreenScaffold(onBack = onBack) {
        HeroCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Участников семьи", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp)
                Text(
                    "${state.familyMembers.size} / 6",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.familyMembers.forEachIndexed { index, member ->
                        Box(
                            modifier = Modifier
                                .padding(start = if (index > 0) (-6).dp else 0.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(member.avatar, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
        state.familyMembers.forEach { member ->
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onOpenMember(member.id) },
                cornerRadius = Radius.md,
                padding = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MemberAvatar(member.avatar)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(member.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "${roleLabel(member.role)} · ${visibilityLabel(member.visibility)}",
                            fontSize = 12.sp,
                            color = TextMuted,
                        )
                    }
                    Text("›", color = TextMuted, fontSize = 18.sp)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(Primary.copy(alpha = 0.06f))
                .border(2.dp, Primary, RoundedCornerShape(Radius.md))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("+ Пригласить", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MemberAvatar(emoji: String, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Primary.copy(alpha = 0.28f), CircleShape)
            .famlySmShadow(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size * 0.52).sp)
    }
}

@Composable
fun FamilyMemberScreen(
    state: FamlyUiState,
    memberId: String,
    onBack: () -> Unit,
    onUpdateRole: (String) -> Unit,
    onUpdateVisibility: (String) -> Unit,
) {
    val member = state.familyMembers.find { it.id == memberId } ?: return
    ScreenScaffold(onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            MemberAvatar(member.avatar, size = 56)
            Text(member.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(Spacing.md))
            Text("Роль", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("admin", "member", "viewer").forEach { role ->
                    FamlyFilterChip(
                        label = roleLabel(role),
                        selected = member.role == role,
                        onClick = { onUpdateRole(role) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
            Text("Видимость", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("full", "partial", "private").forEach { vis ->
                    FamlyFilterChip(
                        label = visibilityLabel(vis),
                        selected = member.visibility == vis,
                        onClick = { onUpdateVisibility(vis) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun BalancesScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onSettle: (fromId: String, toId: String) -> Unit,
) {
    if (!state.settings.hasPremiumAccess()) {
        ScreenScaffold(onBack = onBack) {
            PremiumGateScreen("Балансы IOU", onUpgrade)
        }
        return
    }
    val totalDebt = state.iouBalances.sumOf { it.amountKopecks }
    ScreenScaffold(onBack = onBack) {
        HeroCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤝", fontSize = 40.sp, modifier = Modifier.padding(end = 14.dp))
                Column {
                    Text("Открытые долги", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp)
                    Text(
                        if (state.iouBalances.isEmpty()) "0 ₽" else MoneyFormatter.formatKopecks(totalDebt),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    Text(
                        if (state.iouBalances.isEmpty()) "Все расчёты закрыты"
                        else "${state.iouBalances.size} долг(а) · с учётом взаимных",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (state.iouBalances.isEmpty()) {
            FamlyCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Text("✓", fontSize = 40.sp)
                    Text("Все расчёты закрыты", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
                    Text(
                        "Когда появятся долги между участниками семьи, они отобразятся здесь",
                        textAlign = TextAlign.Center,
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            state.iouBalances.forEach { bal ->
                val from = state.familyMembers.find { it.id == bal.fromId }
                val to = state.familyMembers.find { it.id == bal.toId }
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), cornerRadius = Radius.lg) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MemberAvatar(from?.avatar ?: "👤")
                            Text(from?.name ?: "?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                        }
                        Text("→", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MemberAvatar(to?.avatar ?: "👤")
                            Text(to?.name ?: "?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Expense.copy(alpha = 0.06f))
                            .border(2.dp, Expense.copy(alpha = 0.21f), RoundedCornerShape(Radius.md))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Сумма долга", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Text(MoneyFormatter.formatKopecks(bal.amountKopecks), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Expense)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Primary)
                            .clickable { onSettle(bal.fromId, bal.toId) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Закрыть долг", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplitExpenseScreen(
    state: FamlyUiState,
    transactionId: String,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    if (!state.settings.hasPremiumAccess()) {
        ScreenScaffold(onBack = onBack) {
            PremiumGateScreen("Split расходов", onUpgrade)
        }
        return
    }
    val tx = state.transactions.find { it.id == transactionId } ?: return
    val cat = state.categories.find { it.id == tx.categoryId }
    val existing = tx.splitMemberIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    var selected by remember(transactionId) {
        mutableStateOf(
            if (existing.isNotEmpty()) existing
            else state.familyMembers.map { it.id }.toSet(),
        )
    }

    ScreenScaffold(onBack = onBack) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text("${cat?.icon} ${cat?.name}", fontWeight = FontWeight.Bold)
            Text(MoneyFormatter.formatKopecks(tx.amountKopecks), color = Expense, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(Spacing.md))
            state.familyMembers.forEach { member ->
                FamlyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            selected = if (member.id in selected) selected - member.id else selected + member.id
                        },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(member.avatar, fontSize = 28.sp)
                        Text(member.name, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                        Text(if (member.id in selected) "✓" else "○", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(Primary)
                    .clickable { onSave(selected.toList()) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Сохранить split", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


private fun roleLabel(role: String): String = when (role) {
    "admin" -> "Админ"
    "member" -> "Участник"
    else -> "Наблюдатель"
}

private fun visibilityLabel(v: String): String = when (v) {
    "full" -> "Полный доступ"
    "partial" -> "Частичный"
    else -> "Приватный"
}
