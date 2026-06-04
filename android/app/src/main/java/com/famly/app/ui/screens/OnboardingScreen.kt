package com.famly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.famly.app.ui.theme.Primary

private data class OnboardingSlide(val icon: String, val title: String, val text: String)

private val slides = listOf(
    OnboardingSlide("⚡", "Запись за 3 секунды", "Добавляйте расходы одним тапом. Без сложных форм."),
    OnboardingSlide("📊", "Бюджет под вашу зарплату", "Период с 28-го, safe-to-spend и лимиты — бесплатно."),
    OnboardingSlide("👨‍👩‍👧", "Семья и синхронизация", "Семейный бюджет, sync и split — уже доступны."),
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val slide = slides[step]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = slide.icon, fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = slide.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = slide.text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            slides.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .then(if (i == step) Modifier.fillMaxWidth(0.15f) else Modifier.size(8.dp))
                        .clip(CircleShape)
                        .background(if (i == step) Primary else Color(0xFFE2E8E5)),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (step < slides.lastIndex) step++ else onComplete() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Text(if (step < slides.lastIndex) "Далее" else "Начать")
        }
        if (step < slides.lastIndex) {
            TextButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Пропустить")
            }
        }
    }
}
