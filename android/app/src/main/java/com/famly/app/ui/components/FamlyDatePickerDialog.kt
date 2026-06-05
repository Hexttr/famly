package com.famly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.PrimaryLight
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.TextMuted
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val WEEKDAYS_RU = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

private val MONTHS_NOMINATIVE = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
)

@Composable
fun FamlyDatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now(),
) {
    var selected by remember(initialDate) { mutableStateOf(initialDate) }
    var displayedMonth by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }
    val today = remember { LocalDate.now() }
    val scheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(Radius.xl),
        containerColor = scheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    "Выберите дату",
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    MoneyFormatter.formatHumanDate(selected),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц", tint = Primary)
                    }
                    Text(
                        "${MONTHS_NOMINATIVE[displayedMonth.monthValue - 1]} ${displayedMonth.year}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц", tint = Primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    WEEKDAYS_RU.forEach { label ->
                        Text(
                            label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                monthGrid(displayedMonth).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            DayCell(
                                day = day,
                                month = displayedMonth,
                                selected = day != null && day == selected,
                                isToday = day == today,
                                onClick = { if (day != null) selected = day },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selected)
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Готово", modifier = Modifier.padding(start = 6.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = scheme.onSurfaceVariant,
                )
                Text("Отмена", modifier = Modifier.padding(start = 6.dp), color = scheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun DayCell(
    day: LocalDate?,
    month: YearMonth,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day == null) return@Box
        val inMonth = day.month == month.month
        val bg = when {
            selected -> Primary
            else -> Color.Transparent
        }
        val textColor = when {
            selected -> Color.White
            !inMonth -> TextMuted.copy(alpha = 0.4f)
            else -> scheme.onSurface
        }
        val borderModifier = if (isToday && !selected) {
            Modifier.border(1.5.dp, PrimaryLight, CircleShape)
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(borderModifier)
                .background(bg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (selected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

/** Monday-first grid; `null` = empty cell. */
private fun monthGrid(month: YearMonth): List<List<LocalDate?>> {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val daysInMonth = month.lengthOfMonth()
    val cells = buildList {
        repeat(offset) { add(null) }
        for (d in 1..daysInMonth) {
            add(month.atDay(d))
        }
    }
    val padded = cells + List((7 - cells.size % 7) % 7) { null }
    return padded.chunked(7)
}
