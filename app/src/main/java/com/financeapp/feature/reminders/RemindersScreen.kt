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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.core.utils.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit, vm: RemindersViewModel = hiltViewModel()) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    RequestNotificationPermission()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rem_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(materialIcon("arrow_back"), contentDescription = null) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; sheetOpen = true }) {
                Icon(materialIcon("add"), contentDescription = null)
            }
        },
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyState(
                iconName = "notifications",
                title = stringResource(R.string.rem_empty),
                ctaText = stringResource(R.string.rem_new),
                onCta = { editing = null; sheetOpen = true },
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(reminders, key = { it.id }) { r ->
                    ReminderCard(r, onEdit = { editing = r; sheetOpen = true }, onDelete = { vm.delete(r) })
                }
            }
        }
    }

    if (sheetOpen) {
        AddEditReminderSheet(
            initial = editing,
            defaultCurrency = baseCurrency,
            onDismiss = { sheetOpen = false },
            onSave = { vm.save(it); sheetOpen = false },
        )
    }
}

@Composable
private fun ReminderCard(r: Reminder, onEdit: () -> Unit, onDelete: () -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
        elevation = 0.dp,
        onClick = onEdit,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(materialIcon("notifications"), null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Eyebrow(reminderSubtitle(r))
            }
            r.amount?.let {
                Text(
                    text = CurrencyFormatter.format(it, r.currency ?: Currency.RUB),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(materialIcon("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun reminderSubtitle(r: Reminder): String {
    val date = formatDate(r.dueDate)
    val repeat = when (r.repeat) {
        RepeatType.NONE -> stringResource(R.string.rem_repeat_none)
        RepeatType.WEEKLY -> stringResource(R.string.budget_period_weekly)
        RepeatType.MONTHLY -> stringResource(R.string.budget_period_monthly)
        RepeatType.YEARLY -> stringResource(R.string.budget_period_yearly)
    }
    return "$date · $repeat"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditReminderSheet(
    initial: Reminder?,
    defaultCurrency: Currency,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var dueDate by remember { mutableStateOf(initial?.dueDate ?: DateUtils.startOfDay(System.currentTimeMillis())) }
    var notifyDays by remember { mutableStateOf(initial?.notifyDaysBefore ?: 0) }
    var repeatType by remember { mutableStateOf(initial?.repeat ?: RepeatType.NONE) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(
                text = stringResource(if (initial == null) R.string.rem_new else R.string.rem_title),
                style = MaterialTheme.typography.titleLarge,
            )
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
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(formatDate(dueDate))
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
}

@Composable
private fun <T> PillRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
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
private fun RequestNotificationPermission() {
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

private fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
