package com.famly.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.domain.MoneyFormatter
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.PremiumGateContent
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Spacing

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

@Composable
fun FamilyScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenMember: (String) -> Unit,
) {
    if (!state.settings.hasPremiumAccess()) {
        ScreenScaffold(title = "Семья", onBack = onBack) {
            PremiumGateScreen("Семейный бюджет", onUpgrade)
        }
        return
    }
    ScreenScaffold(title = "Семья", onBack = onBack) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                Text("Участников семьи", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp)
                Text(
                    "${state.familyMembers.size} / 6",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            state.familyMembers.forEach { member ->
                FamlyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                        .clickable { onOpenMember(member.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(member.avatar, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${roleLabel(member.role)} · ${visibilityLabel(member.visibility)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                        Text("›", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Пригласить")
            }
        }
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
    ScreenScaffold(title = member.name, onBack = onBack) {
        Column(modifier = Modifier.padding(Spacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(member.avatar, fontSize = 56.sp)
            Text(member.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.md))
            Text("Роль", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("admin", "member", "viewer").forEach { role ->
                    FilterChip(
                        selected = member.role == role,
                        onClick = { onUpdateRole(role) },
                        label = { Text(roleLabel(role)) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
            Text("Видимость", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("full", "partial", "private").forEach { vis ->
                    FilterChip(
                        selected = member.visibility == vis,
                        onClick = { onUpdateVisibility(vis) },
                        label = { Text(visibilityLabel(vis)) },
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
        ScreenScaffold(title = "Кто кому должен", onBack = onBack) {
            PremiumGateScreen("Балансы IOU", onUpgrade)
        }
        return
    }
    ScreenScaffold(title = "Кто кому должен", onBack = onBack) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            if (state.iouBalances.isEmpty()) {
                FamlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Все долги закрыты 🎉", fontWeight = FontWeight.SemiBold)
                }
            }
            state.iouBalances.forEach { bal ->
                val from = state.familyMembers.find { it.id == bal.fromId }
                val to = state.familyMembers.find { it.id == bal.toId }
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Text("${from?.name ?: "?"} должен(а) ${to?.name ?: "?"}")
                    Text(
                        MoneyFormatter.formatKopecks(bal.amountKopecks),
                        fontWeight = FontWeight.Bold,
                        color = Expense,
                        fontSize = 20.sp,
                    )
                    Button(
                        onClick = { onSettle(bal.fromId, bal.toId) },
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("Закрыть долг")
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
        ScreenScaffold(title = "Split расхода", onBack = onBack) {
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

    ScreenScaffold(title = "Split расхода", onBack = onBack) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(cat?.icon ?: "📝", fontSize = 32.sp)
                Text(
                    MoneyFormatter.formatKopecks(tx.amountKopecks),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Expense,
                )
                Text(cat?.name ?: "—", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            val shareCount = selected.size.coerceAtLeast(1)
            val share = tx.amountKopecks / shareCount
            state.familyMembers.forEach { member ->
                val checked = member.id in selected
                FamlyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                        .clickable {
                            selected = if (checked) selected - member.id else selected + member.id
                        },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (checked) "☑" else "☐", modifier = Modifier.padding(end = 8.dp))
                        Text(member.avatar, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(member.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        if (checked) {
                            Text(MoneyFormatter.formatKopecks(share), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Button(
                onClick = { onSave(selected.toList()) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("Сохранить split")
            }
        }
    }
}
