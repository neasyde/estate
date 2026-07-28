package com.financeapp.feature.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.accentChipColors
import com.financeapp.core.utils.DateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderSheet(
    initial: Reminder?,
    defaultCurrency: Currency,
    defaultNotifyHour: Int = 9,
    defaultNotifyDays: Int = 0,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var dueDate by remember { mutableStateOf(initial?.dueDate ?: DateUtils.startOfDay(System.currentTimeMillis())) }
    var minuteOfDay by remember { mutableStateOf(initial?.notifyMinuteOfDay ?: defaultNotifyHour * 60) }
    var notifyDays by remember { mutableStateOf(initial?.notifyDaysBefore ?: defaultNotifyDays) }
    var repeatType by remember { mutableStateOf(initial?.repeat ?: RepeatType.NONE) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    RequestNotificationPermission()

    val localeContext = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (initial == null) R.string.rem_new else R.string.rem_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                if (initial != null && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        MaterialIcon(
                            symbol("delete"),
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // — Details section —
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.rem_field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.rem_amount_optional)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (amountText.toDoubleOrNull() != null) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    LazyRow(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Currency.entries.size) { index ->
                            val c = Currency.entries[index]
                            FilterChip(
                                selected = c == currency,
                                onClick = { currency = c },
                                label = { Text("${c.symbol} ${c.code}", maxLines = 1) },
                                colors = accentChipColors(),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // — Date & Time section —
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        MaterialIcon(symbol("calendar_today"), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.rem_due_date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatReminderDate(dueDate), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Surface(
                    onClick = { showTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        MaterialIcon(symbol("schedule"), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.rem_time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatReminderTime(minuteOfDay), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // — Remind Before section —
            FieldLabel(stringResource(R.string.rem_notify_before))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 3, 7).forEach { n ->
                    FilterChip(
                        selected = notifyDays == n,
                        onClick = { notifyDays = n },
                        label = {
                            Text(
                                text = if (n == 0) stringResource(R.string.rem_on_day) else pluralStringResource(R.plurals.rem_days, n, n),
                                maxLines = 1,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = accentChipColors(),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // — Repeat section —
            FieldLabel(stringResource(R.string.rem_repeat))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    RepeatType.NONE to stringResource(R.string.rem_repeat_none),
                    RepeatType.WEEKLY to stringResource(R.string.budget_period_weekly),
                    RepeatType.MONTHLY to stringResource(R.string.budget_period_monthly),
                    RepeatType.YEARLY to stringResource(R.string.budget_period_yearly),
                ).forEach { (rt, label) ->
                    FilterChip(
                        selected = repeatType == rt,
                        onClick = { repeatType = rt },
                        label = { Text(label, maxLines = 1) },
                        modifier = Modifier.weight(1f),
                        colors = accentChipColors(),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val amount = amountText.toDoubleOrNull()
                    onSave(
                        Reminder(
                            id = initial?.id ?: 0,
                            title = title.trim(),
                            amount = amount,
                            currency = if (amount != null) currency else null,
                            dueDate = dueDate,
                            notifyDaysBefore = notifyDays,
                            repeat = repeatType,
                            notifyMinuteOfDay = minuteOfDay,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    stringResource(R.string.action_save),
                    style = MaterialTheme.typography.titleMedium,
                )
        }
        }
    }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = minuteOfDay / 60,
            initialMinute = minuteOfDay % 60,
            is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    minuteOfDay = state.hour * 60 + state.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            title = { Text(stringResource(R.string.rem_time)) },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun reminderSubtitle(r: Reminder): String {
    val date = formatReminderDate(r.dueDate)
    val time = formatReminderTime(r.notifyMinuteOfDay)
    val repeat = when (r.repeat) {
        RepeatType.NONE -> null
        RepeatType.WEEKLY -> stringResource(R.string.budget_period_weekly)
        RepeatType.MONTHLY -> stringResource(R.string.budget_period_monthly)
        RepeatType.YEARLY -> stringResource(R.string.budget_period_yearly)
    }
    return if (repeat == null) "$date, $time" else "$date, $time · $repeat"
}

internal fun formatReminderDate(millis: Long): String = DateUtils.mediumDate(millis)

internal fun formatReminderTime(minuteOfDay: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60)

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
