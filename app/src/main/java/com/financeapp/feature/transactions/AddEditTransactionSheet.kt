package com.financeapp.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    editId: Long?,
    presetType: TransactionType?,
    defaultCurrency: Currency,
    onDismiss: () -> Unit,
    vm: AddEditTransactionViewModel = hiltViewModel(),
) {
    val form by vm.form.collectAsStateWithLifecycle()
    val categories by vm.categoriesForType.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(editId) { vm.load(editId, defaultCurrency, presetType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(
                stringResource(if (form.id == null) R.string.form_new_tx else R.string.form_edit_tx),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(stringResource(R.string.tx_filter_expense), form.type == TransactionType.EXPENSE) { vm.setType(TransactionType.EXPENSE) }
                TypeChip(stringResource(R.string.tx_filter_income), form.type == TransactionType.INCOME) { vm.setType(TransactionType.INCOME) }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = form.amount,
                onValueChange = vm::setAmount,
                label = { Text(stringResource(R.string.form_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { c ->
                    FilterChip(
                        selected = form.currency == c,
                        onClick = { vm.setCurrency(c) },
                        label = { Text("${c.symbol} ${c.code}") },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.form_category), style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories, key = { it.id }) { cat ->
                    val sel = form.categoryId == cat.id
                    val interaction = remember { MutableInteractionSource() }
                    Column(
                        // No indication: a default ripple here draws an ugly rectangular hitbox
                        // around the icon+label. Selection is shown by the icon growing instead.
                        modifier = Modifier
                            .clickable(interactionSource = interaction, indication = null) { vm.setCategory(cat.id) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryIcon(cat, size = if (sel) 52.dp else 44.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            categoryDisplayName(cat),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = form.note,
                onValueChange = vm::setNote,
                label = { Text(stringResource(R.string.form_note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))

            val dateText = remember(form.date) {
                Instant.ofEpochMilli(form.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            }
            val timeText = remember(form.date) {
                Instant.ofEpochMilli(form.date).atZone(ZoneId.systemDefault()).toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showDate = true }, modifier = Modifier.weight(1f)) {
                    Text("${stringResource(R.string.form_date)}: $dateText")
                }
                OutlinedButton(onClick = { showTime = true }, modifier = Modifier.weight(1f)) {
                    Text(timeText)
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = form.recurring, onCheckedChange = vm::setRecurring)
                Text(stringResource(R.string.form_recurring))
            }
            if (form.recurring) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    IntervalChip(stringResource(R.string.form_interval_daily), form.interval == IntervalType.DAILY) { vm.setInterval(IntervalType.DAILY) }
                    IntervalChip(stringResource(R.string.form_interval_weekly), form.interval == IntervalType.WEEKLY) { vm.setInterval(IntervalType.WEEKLY) }
                    IntervalChip(stringResource(R.string.form_interval_monthly), form.interval == IntervalType.MONTHLY) { vm.setInterval(IntervalType.MONTHLY) }
                    IntervalChip(stringResource(R.string.form_interval_yearly), form.interval == IntervalType.YEARLY) { vm.setInterval(IntervalType.YEARLY) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.form_auto_add))
                    Spacer(Modifier.weight(1f))
                    Switch(checked = form.autoAdd, onCheckedChange = vm::setAutoAdd)
                }
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.action_save)) }
        }
    }

    if (showDate) {
        val dateState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = form.date)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let(vm::setDate)
                    showDate = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(state = dateState) }
    }

    if (showTime) {
        val zdt = Instant.ofEpochMilli(form.date).atZone(ZoneId.systemDefault())
        val timeState = androidx.compose.material3.rememberTimePickerState(
            initialHour = zdt.hour, initialMinute = zdt.minute, is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = { vm.setTime(timeState.hour, timeState.minute); showTime = false }) {
                    Text(stringResource(R.string.action_done))
                }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text(stringResource(R.string.action_cancel)) } },
            text = {
                androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.TimePicker(state = timeState)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = Modifier.width(140.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
