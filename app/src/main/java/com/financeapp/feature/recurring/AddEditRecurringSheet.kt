package com.financeapp.feature.recurring

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.components.CalculatorSheet
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.accentChipColors
import com.financeapp.core.utils.rememberHaptics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringSheet(
    onDismiss: () -> Unit,
    onSave: (templateJson: String, interval: IntervalType, nextDate: Long, templateTransactionId: Long?) -> Unit = { _, _, _, _ -> },
    initial: com.financeapp.core.data.local.entity.RecurringRuleEntity? = null,
    baseCurrency: Currency = Currency.RUB,
    json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialTemplate = remember(initial) {
        initial?.templateJson?.let {
            runCatching { json.decodeFromString<RecurringTemplate>(it) }.getOrNull()
        }
    }
    var type by remember { mutableStateOf(initialTemplate?.type?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() } ?: TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf(initialTemplate?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(initialTemplate?.note ?: "") }
    var currency by remember { mutableStateOf(initialTemplate?.currency?.let { runCatching { Currency.valueOf(it) }.getOrNull() } ?: baseCurrency) }
    var interval by remember { mutableStateOf(initial?.intervalType?.let { runCatching { IntervalType.valueOf(it) }.getOrNull() } ?: IntervalType.MONTHLY) }
    var showCalc by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()

    val localeContext = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.background) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(if (initial != null) R.string.recurring_edit else R.string.recurring_new),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(20.dp))

            Eyebrow(stringResource(R.string.dash_add_type))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TransactionType.EXPENSE to stringResource(R.string.dash_add_expense), TransactionType.INCOME to stringResource(R.string.dash_add_income)).forEach { (t, label) ->
                    FilterChip(selected = type == t, onClick = { type = t }, label = { Text(label) }, modifier = Modifier.weight(1f), colors = accentChipColors())
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    val firstDot = filtered.indexOf('.')
                    amount = if (firstDot == -1) filtered
                    else filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
                    amountError = false
                },
                label = { Text(stringResource(R.string.tx_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {{ Text(stringResource(R.string.form_amount_required)) }} else null,
                leadingIcon = { Text(currency.symbol, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp)) },
                trailingIcon = {
                    IconButton(onClick = { showCalc = true }) {
                        MaterialIcon(symbol("calculate"), contentDescription = null)
                    }
                },
            )
            Spacer(Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(Currency.entries) { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text("${c.symbol} ${c.code}") },
                            colors = accentChipColors(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.tx_note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.recurring_interval))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    IntervalType.DAILY to stringResource(R.string.interval_daily),
                    IntervalType.WEEKLY to stringResource(R.string.interval_weekly),
                    IntervalType.MONTHLY to stringResource(R.string.interval_monthly),
                    IntervalType.YEARLY to stringResource(R.string.interval_yearly),
                ).forEach { (i, label) ->
                    FilterChip(
                        selected = interval == i,
                        onClick = { interval = i },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp)) },
                        modifier = Modifier.weight(1f),
                        colors = accentChipColors(),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            androidx.compose.material3.Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0) {
                        amountError = true
                        haptics(false)
                        return@Button
                    }
                    val next = System.currentTimeMillis() + intervalMillis(interval)
                    val template = RecurringTemplate(
                        amount = amountVal,
                        currency = currency.name,
                        type = type.name,
                        categoryId = initialTemplate?.categoryId,
                        note = note.ifBlank { null },
                    )
                    haptics(true)
                    onSave(json.encodeToString(template), interval, next, initial?.templateTransactionId)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
            Spacer(Modifier.height(16.dp))
        }
        }
    }

    if (showCalc) {
        CalculatorSheet(
            value = amount,
            onValueChange = { amount = it },
            currencySymbol = currency.symbol,
            onDismiss = { showCalc = false },
        )
    }
}

private fun intervalMillis(interval: IntervalType): Long = when (interval) {
    IntervalType.DAILY -> 86_400_000L
    IntervalType.WEEKLY -> 7 * 86_400_000L
    IntervalType.MONTHLY -> 30L * 86_400_000L
    IntervalType.YEARLY -> 365L * 86_400_000L
}
