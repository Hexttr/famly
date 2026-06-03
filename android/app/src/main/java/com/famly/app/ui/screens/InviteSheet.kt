package com.famly.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.ui.components.FamlyCard
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSheet(
    visible: Boolean,
    inviteCode: String?,
    inviteUrl: String?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit,
) {
    if (!visible) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.lg)) {
            Text("Пригласить в семью", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Поделитесь кодом или ссылкой с близкими",
                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.md),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            if (inviteCode != null) {
                FamlyCard(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) {
                    Text("Код приглашения", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 12.sp)
                    Text(inviteCode, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(vertical = 6.dp))
                    inviteUrl?.let {
                        Text("Ссылка", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        Text(it, fontSize = 13.sp, color = Primary)
                    }
                }
            }
            FamlyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !loading, onClick = onGenerate),
            ) {
                Text(
                    when {
                        loading -> "Генерация…"
                        inviteCode == null -> "Создать код приглашения"
                        else -> "Обновить код"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }
        }
    }
}
