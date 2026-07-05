package com.financeapp.feature.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Bottom sheet for creating/editing a reminder. Shared by the Reminders screen and Dashboard. */
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

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (initial == null) R.string.rem_new else R.string.rem_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                // Delete is offered only when editing an existing reminder.
                if (initial != null && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            materialIcon("delete"),
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.rem_field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

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
                PillRow(
                    options = Currency.entries.map { it to "${it.symbol} ${it.code}" },
                    selected = currency,
                    onSelect = { currency = it },
                )
            }
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.rem_due_date))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(materialIcon("notifications"), null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatReminderDate(dueDate))
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(formatReminderTime(minuteOfDay))
                }
            }
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.rem_notify_before))
            Spacer(Modifier.height(8.dp))
            PillRow(
                options = listOf(0, 1, 3, 7).map { n ->
                    n to if (n == 0) stringResource(R.string.rem_on_day) else stringResource(R.string.rem_days_abbrev, n)
                },
                selected = notifyDays,
                onSelect = { notifyDays = it },
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.rem_repeat))
            Spacer(Modifier.height(8.dp))
            PillRow(
                options = listOf(
                    RepeatType.NONE to stringResource(R.string.rem_repeat_none),
                    RepeatType.WEEKLY to stringResource(R.string.budget_period_weekly),
                    RepeatType.MONTHLY to stringResource(R.string.budget_period_monthly),
                    RepeatType.YEARLY to stringResource(R.string.budget_period_yearly),
                ),
                selected = repeatType,
                onSelect = { repeatType = it },
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
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
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
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
internal fun <T> PillRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier.weight(1f).clip(PillShape)
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
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

internal fun formatReminderDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

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
