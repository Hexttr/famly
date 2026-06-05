package com.famly.app.ui.components

import android.content.res.Configuration
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamlyDatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now(),
) {
    val context = LocalContext.current
    val russianLocale = Locale.forLanguageTag("ru-RU")
    val russianConfiguration = Configuration(context.resources.configuration).apply {
        setLocale(russianLocale)
    }
    val localizedContext = context.createConfigurationContext(russianConfiguration)
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
    )

    CompositionLocalProvider(
        LocalConfiguration provides russianConfiguration,
        LocalContext provides localizedContext,
    ) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onConfirm(
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            )
                        }
                        onDismissRequest()
                    },
                ) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Отмена")
                }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = { Text("Выберите дату") },
            )
        }
    }
}
