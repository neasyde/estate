package com.financeapp.feature.transactions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CalculatorSheet
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.accentChipColors
import com.financeapp.core.utils.DateUtils
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

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
    var showTime by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showCalc by remember { mutableStateOf(false) }

    LaunchedEffect(editId) { vm.load(editId, defaultCurrency, presetType) }

    val localeContext = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(
            Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(if (form.id == null) R.string.form_new_tx else R.string.form_edit_tx),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(stringResource(R.string.tx_filter_expense), form.type == TransactionType.EXPENSE, modifier = Modifier.weight(1f)) { vm.setType(TransactionType.EXPENSE) }
                TypeChip(stringResource(R.string.tx_filter_income), form.type == TransactionType.INCOME, modifier = Modifier.weight(1f)) { vm.setType(TransactionType.INCOME) }
            }
            Spacer(Modifier.height(10.dp))

            val amountColor = when (form.type) {
                TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                TransactionType.INCOME -> com.financeapp.core.ui.theme.IncomeGreen
            }
            OutlinedTextField(
                value = form.amount,
                onValueChange = vm::setAmount,
                label = { Text(stringResource(R.string.form_amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = amountColor),
                leadingIcon = { Text(form.currency.symbol, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp)) },
                trailingIcon = {
                    IconButton(onClick = { showCalc = true }) {
                        MaterialIcon(symbol("calculate"), contentDescription = null)
                    }
                },
            )
            Spacer(Modifier.height(6.dp))

            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(Currency.entries) { c ->
                        FilterChip(
                            selected = form.currency == c,
                            onClick = { vm.setCurrency(c) },
                            label = { Text("${c.symbol} ${c.code}") },
                            colors = accentChipColors(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Hairline()
            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.form_category), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
                items(categories, key = { it.id }) { cat ->
                    val sel = form.categoryId == cat.id
                    val interaction = remember { MutableInteractionSource() }
                    val scale by animateFloatAsState(
                        targetValue = if (sel) 1.18f else 1f,
                        animationSpec = if (reducedMotion()) tween(0) else tween(Motion.Short),
                        label = "iconScale",
                    )
                    Column(
                        modifier = Modifier
                            .clickable(interactionSource = interaction, indication = null) { vm.setCategory(cat.id) }
                            .pressScale(interaction)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryIcon(cat, size = 44.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            categoryDisplayName(cat),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Hairline()
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = form.note,
                onValueChange = vm::setNote,
                label = { Text(stringResource(R.string.form_note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))

            val dateText = remember(form.date) { DateUtils.mediumDate(form.date) }
            val timeText = remember(form.date) { DateUtils.timeLabel(form.date) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = { showDate = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(0.5f).height(56.dp),
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MaterialIcon(symbol("calendar_today"), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(dateText, style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                        }
                    }
                }
                Surface(
                    onClick = { showTime = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(0.5f).height(56.dp),
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MaterialIcon(symbol("schedule"), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(timeText, style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            val recurringInteraction = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = recurringInteraction, indication = null) { vm.setRecurring(!form.recurring) }
                    .pressScale(recurringInteraction)
                    .padding(vertical = 4.dp),
            ) {
                Checkbox(checked = form.recurring, onCheckedChange = vm::setRecurring)
                Text(stringResource(R.string.form_recurring), style = MaterialTheme.typography.bodyMedium)
            }
            if (form.recurring) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                ) {
                    item { IntervalChip(stringResource(R.string.form_interval_daily), form.interval == IntervalType.DAILY) { vm.setInterval(IntervalType.DAILY) } }
                    item { IntervalChip(stringResource(R.string.form_interval_weekly), form.interval == IntervalType.WEEKLY) { vm.setInterval(IntervalType.WEEKLY) } }
                    item { IntervalChip(stringResource(R.string.form_interval_monthly), form.interval == IntervalType.MONTHLY) { vm.setInterval(IntervalType.MONTHLY) } }
                    item { IntervalChip(stringResource(R.string.form_interval_yearly), form.interval == IntervalType.YEARLY) { vm.setInterval(IntervalType.YEARLY) } }
                }
            }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
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

    if (showCalc) {
        CalculatorSheet(
            value = form.amount,
            onValueChange = vm::setAmount,
            currencySymbol = form.currency.symbol,
            onDismiss = { showCalc = false },
        )
    }

    if (showTime) {
        val zdt = Instant.ofEpochMilli(form.date).atZone(ZoneId.systemDefault())
        val timeState = androidx.compose.material3.rememberTimePickerState(
            initialHour = zdt.hour, initialMinute = zdt.minute, is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTime = false },
            containerColor = MaterialTheme.colorScheme.background,
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

    if (showDate) {
        val zdt = Instant.ofEpochMilli(form.date).atZone(ZoneId.systemDefault())
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = zdt.toLocalDate().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { vm.setDate(it) }
                    showDate = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) {
            DatePicker(state = dateState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier, colors = accentChipColors())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = accentChipColors(),
    )
}
