package com.famly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.PrimaryDark
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing

@Composable
fun FamlyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Primary.copy(alpha = 0.27f),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(Radius.lg))
            .padding(Spacing.md),
        content = content,
    )
}

@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    gradientStart: Color = Primary,
    gradientEnd: Color = PrimaryDark,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.xl))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
            .padding(Spacing.md),
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionHeading(icon: String, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Primary.copy(alpha = 0.08f))
                .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Text(
            title,
            modifier = Modifier.padding(start = Spacing.sm),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun TrialBanner(
    trialDaysLeft: Int,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onUpgrade: () -> Unit = {},
) {
    if (isPremium || trialDaysLeft <= 0) return
    FamlyCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.sm),
        borderColor = Premium.copy(alpha = 0.4f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
                Text("Пробный Премиум", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Осталось $trialDaysLeft дн. · семья, split, аналитика",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = Premium),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Подробнее", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PremiumGateContent(
    feature: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⭐", fontSize = 40.sp)
        Text("Премиум", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "$feature доступна в Премиум",
            modifier = Modifier.padding(vertical = Spacing.sm),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Button(
            onClick = onUpgrade,
            colors = ButtonDefaults.buttonColors(containerColor = Premium),
        ) {
            Text("Попробовать Премиум")
        }
    }
}
