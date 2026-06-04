package com.famly.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.famly.app.domain.FamlyAccess
import com.famly.app.domain.MoneyFormatter
import com.famly.app.ui.FamlyUiState
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.components.FamlyFilterChip
import com.famly.app.ui.components.HeroCard
import com.famly.app.ui.components.QrCodeImage
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.Warning
import com.famly.app.ui.theme.famlySmShadow

@Composable
fun FamilyScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenMember: (String) -> Unit,
    onSetupFamily: (String) -> Unit,
    onJoinHousehold: (String) -> Unit,
    onRefreshInvite: () -> Unit,
    onOpenSettings: () -> Unit,
    inviteCode: String?,
    inviteUrl: String?,
    inviteLoading: Boolean,
    inviteError: String?,
    initialJoinCode: String = "",
) {
    if (!FamlyAccess.hasPremium(state.settings)) {
        ScreenScaffold(onBack = onBack) {
            PremiumGateScreen("Семейный бюджет", onUpgrade)
        }
        return
    }

    val familyCreated = state.settings.householdName != null || state.familyMembers.isNotEmpty() || state.settings.isSynced
    var familyName by remember(state.settings.householdName) {
        mutableStateOf(state.settings.householdName ?: "")
    }
    var joinCode by remember(initialJoinCode) { mutableStateOf(initialJoinCode) }
    val context = LocalContext.current
    val displayName = state.settings.householdName?.takeIf { it.isNotBlank() } ?: familyName.takeIf { it.isNotBlank() }

    LaunchedEffect(state.settings.isSynced, inviteCode, inviteLoading) {
        if (state.settings.isSynced && inviteCode == null && !inviteLoading) {
            onRefreshInvite()
        }
    }

    ScreenScaffold(onBack = onBack) {
        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Фамилия / название семьи") },
            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
            placeholder = { Text("Например, Ивановы") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.md),
            singleLine = true,
            enabled = !familyCreated || !state.settings.isSynced,
        )

        if (!familyCreated && state.settings.isAuthenticated) {
            OutlinedTextField(
                value = joinCode,
                onValueChange = { joinCode = it },
                label = { Text("Код приглашения") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
            )
            Button(
                onClick = { onJoinHousehold(joinCode.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                enabled = joinCode.isNotBlank() && !inviteLoading,
            ) {
                Text("Присоединиться к семье")
            }
        }

        if (!familyCreated) {
            Button(
                onClick = { onSetupFamily(familyName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                enabled = !inviteLoading && familyName.isNotBlank(),
            ) {
                Text(if (inviteLoading) "Создание…" else "Создать семью")
            }
        }

        if (!state.settings.isAuthenticated) {
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md)
                    .clickable { onOpenSettings() },
                padding = 14.dp,
            ) {
                Text("⚠️", fontSize = 20.sp)
                Text(
                    "Зарегистрируйтесь в Настройках, чтобы не потерять семью и синхронизировать бюджет между устройствами.",
                    fontSize = 13.sp,
                    color = Warning,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    "Перейти в Настройки →",
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        inviteError?.let {
            Text(it, color = Expense, fontSize = 13.sp, modifier = Modifier.padding(bottom = Spacing.sm))
        }

        if (familyCreated || state.settings.isSynced) {
            HeroCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    displayName?.let {
                        Text(it, color = Color.White.copy(alpha = 0.92f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Участников семьи", color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "${state.familyMembers.size.coerceAtLeast(if (familyCreated) 1 else 0)} / 6",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
                    )
                    if (state.familyMembers.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            state.familyMembers.forEachIndexed { index, member ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = if (index > 0) (-6 * index).dp else 0.dp)
                                        .zIndex((state.familyMembers.size - index).toFloat())
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

        if (state.settings.isAuthenticated && (familyCreated || state.settings.isSynced)) {
            Text(
                "Пригласить участников",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            if (inviteLoading && inviteCode == null) {
                Text("Генерация ссылки…", color = TextMuted, fontSize = 14.sp)
            } else if (inviteCode != null) {
                val link = inviteUrl ?: "famly://join?code=$inviteCode"
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm), padding = 16.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        QrCodeImage(
                            content = link,
                            modifier = Modifier
                                .size(180.dp)
                                .padding(bottom = 12.dp),
                            sizePx = 512,
                        )
                        Text("Код: $inviteCode", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                FamlyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                        .clickable { copyToClipboard(context, link) },
                    padding = 14.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ссылка для приглашения", fontSize = 12.sp, color = TextMuted)
                            Text(link, fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ContentCopy, contentDescription = "Копировать", tint = Primary)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Primary.copy(alpha = 0.08f))
                            .border(2.dp, Primary, RoundedCornerShape(Radius.md))
                            .clickable { copyToClipboard(context, inviteCode) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Копировать код", color = Primary, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Primary)
                            .clickable {
                                val shareText = buildString {
                                    append("Присоединяйся к семье «${displayName ?: "наша семья"}» в Мой (Наш) Бюджет!\n")
                                    append("Код: $inviteCode\n")
                                    append("Ссылка: $link")
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        },
                                        "Поделиться приглашением",
                                    ),
                                )
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Поделиться", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (!inviteLoading) {
                Button(onClick = onRefreshInvite, modifier = Modifier.fillMaxWidth()) {
                    Text("Создать ссылку приглашения")
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("invite", text))
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
    onCycleAvatar: () -> Unit,
) {
    val member = state.familyMembers.find { it.id == memberId } ?: return
    ScreenScaffold(onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clickable { onCycleAvatar() }
                    .padding(bottom = 4.dp),
            ) {
                MemberAvatar(member.avatar, size = 72)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Сменить аватар", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Text("Нажмите на аватар, чтобы сменить", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(bottom = 8.dp))
            Text(member.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Text("Роль", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                RoleChip("admin", "Админ", Icons.Default.AdminPanelSettings, member.role, onUpdateRole)
                RoleChip("member", "Участник", Icons.Default.Person, member.role, onUpdateRole)
                RoleChip("viewer", "Наблюдатель", Icons.Default.Visibility, member.role, onUpdateRole)
            }
            FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg), padding = 12.dp) {
                Text("Админ — управляет семьёй и видит все операции.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                Text("Участник — добавляет и редактирует свои операции.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                Text("Наблюдатель — только просмотр, без изменений.", fontSize = 13.sp, color = TextMuted)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Text("Видимость", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                VisibilityChip("full", "Полный доступ", Icons.Default.Visibility, member.visibility, onUpdateVisibility)
                VisibilityChip("partial", "Частичный", Icons.Default.VisibilityOff, member.visibility, onUpdateVisibility)
                VisibilityChip("private", "Приватный", Icons.Default.Lock, member.visibility, onUpdateVisibility)
            }
            FamlyCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                Text("Полный доступ — все операции участника видны семье.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                Text("Частичный — видны только общие расходы.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                Text("Приватный — операции скрыты от других.", fontSize = 13.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun RoleChip(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: String,
    onSelect: (String) -> Unit,
) {
    FamlyFilterChip(
        label = label,
        selected = selected == value,
        onClick = { onSelect(value) },
        modifier = Modifier.padding(end = 4.dp),
        leading = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
    )
}

@Composable
private fun VisibilityChip(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: String,
    onSelect: (String) -> Unit,
) {
    FamlyFilterChip(
        label = label,
        selected = selected == value,
        onClick = { onSelect(value) },
        modifier = Modifier.padding(end = 4.dp),
        leading = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
    )
}

@Composable
fun BalancesScreen(
    state: FamlyUiState,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onSettle: (fromId: String, toId: String) -> Unit,
) {
    if (!FamlyAccess.hasPremium(state.settings)) {
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
    if (!FamlyAccess.hasPremium(state.settings)) {
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
