package com.financeapp.feature.recurring

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onBack: () -> Unit,
    vm: RecurringViewModel = hiltViewModel(),
) {
    val rules by vm.rules.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    var addSheetOpen by remember { mutableStateOf(false) }
    var editingRuleId by remember { mutableStateOf<Long?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    // Stats
    val enabledCount = rules.count { it.enabled }
    val disabledCount = rules.size - enabledCount
    val totalMonthly = remember(rules) {
        rules.filter { it.enabled }.sumOf { rule ->
            val template = runCatching { json.decodeFromString<RecurringTemplate>(rule.templateJson) }.getOrNull()
            val amount = template?.amount ?: 0.0
            val interval = rule.intervalType
            when (interval) {
                "DAILY" -> amount * 30
                "WEEKLY" -> amount * 52.0 / 12.0
                "MONTHLY" -> amount
                "YEARLY" -> amount / 12
                else -> amount
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recurring_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        MaterialIcon(symbol("arrow_back"), contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingRuleId = null; addSheetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                MaterialIcon(symbol("add"), contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            
            // Stats summary
            if (rules.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = stringResource(R.string.recurring_active),
                        value = enabledCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.recurring_monthly),
                        value = CurrencyFormatter.format(totalMonthly, baseCurrency, LocalShowDecimals.current),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (rules.isEmpty()) {
                EmptyState(
                    iconName = "repeat",
                    title = stringResource(R.string.recurring_empty),
                )
            } else {
                LazyColumn {
                    items(rules, key = { it.id }) { rule ->
                        val template = remember(rule.templateJson) {
                            runCatching { json.decodeFromString<RecurringTemplate>(rule.templateJson) }.getOrNull()
                        }
                        val txType = runCatching {
                            TransactionType.valueOf(template?.type ?: "EXPENSE")
                        }.getOrDefault(TransactionType.EXPENSE)
                        val currency = runCatching {
                            Currency.valueOf(template?.currency ?: "RUB")
                        }.getOrDefault(Currency.RUB)
                        val nextDateStr = remember(rule.nextDate) {
                            DateUtils.mediumDate(rule.nextDate)
                        }

                        Column {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Type icon
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    MaterialIcon(
                                        symbol("repeat"),
                                        tint = if (txType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                
                                Column(Modifier.weight(1f)) {
                                    // Amount and interval
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        template?.let { t ->
                                            Text(
                                                CurrencyFormatter.format(t.amount, currency, LocalShowDecimals.current),
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (txType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(
                                            intervalLabel(rule.intervalType),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    
                                    // Note
                                    template?.let { t ->
                                        if (!t.note.isNullOrBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                t.note, 
                                                style = MaterialTheme.typography.bodySmall, 
                                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    
                                    // Next date
                                    Spacer(Modifier.height(4.dp))
                                    Eyebrow("${stringResource(R.string.recurring_next_date)}: $nextDateStr")
                                }
                                
                                // Enable/disable toggle
                                Switch(
                                    checked = rule.enabled,
                                    onCheckedChange = {
                                        vm.toggleEnabled(rule)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                )
                                
                                IconButton(onClick = { editingRuleId = rule.id; addSheetOpen = true }) {
                                    MaterialIcon(symbol("edit"), contentDescription = stringResource(R.string.action_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { deleteConfirmId = rule.id }) {
                                    MaterialIcon(symbol("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            Hairline(Modifier.padding(horizontal = 20.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(96.dp))
        }
    }

    if (addSheetOpen) {
        val editingRule = if (editingRuleId != null) rules.find { it.id == editingRuleId } else null
        AddEditRecurringSheet(
            initial = editingRule,
            baseCurrency = baseCurrency,
            json = vm.sharedJson,
            onDismiss = { addSheetOpen = false },
            onSave = { template, interval, nextDate, templateTxId ->
                if (editingRule != null) {
                    vm.updateRule(editingRule, template, interval, nextDate, editingRule.enabled)
                } else {
                    scope.launch { vm.addRule(template, interval, nextDate, false) }
                }
                addSheetOpen = false
            },
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.recurring_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.delete(id); deleteConfirmId = null }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        Modifier
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Eyebrow(label)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun intervalLabel(type: String): String {
    val resId = when (type) {
        "DAILY" -> R.string.interval_daily
        "WEEKLY" -> R.string.interval_weekly
        "MONTHLY" -> R.string.interval_monthly
        "YEARLY" -> R.string.interval_yearly
        else -> R.string.interval_monthly
    }
    return stringResource(resId)
}
