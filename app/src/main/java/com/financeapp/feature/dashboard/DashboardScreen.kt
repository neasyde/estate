package com.financeapp.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DayAmount
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.anim.animatedCountUp
import com.financeapp.core.ui.components.AmountText
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.TransactionRow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.feature.transactions.AddEditTransactionSheet
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onSeeAll: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val data by vm.state.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    val hidden by vm.balanceHidden.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var presetType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var fabExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            BalanceCard(data.balanceBase, baseCurrency, hidden, vm::toggleBalanceHidden)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyColumn(stringResource(R.string.dash_income), data.monthIncomeBase, baseCurrency, true, Modifier.weight(1f))
                MoneyColumn(stringResource(R.string.dash_expense), data.monthExpenseBase, baseCurrency, false, Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.dash_last7), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Last7Chart(data.last7Days)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.dash_recent), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (data.recent.isEmpty()) {
                EmptyState(iconName = "wallet", title = stringResource(R.string.dash_empty))
            } else {
                data.recent.forEachIndexed { i, item ->
                    Staggered(i) { TransactionRow(item) }
                }
                TextButton(onClick = onSeeAll, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dash_see_all))
                }
            }
            Spacer(Modifier.height(80.dp))
        }

        ExpandableFab(
            expanded = fabExpanded,
            onToggle = { fabExpanded = !fabExpanded },
            onExpense = { presetType = TransactionType.EXPENSE; sheetOpen = true; fabExpanded = false },
            onIncome = { presetType = TransactionType.INCOME; sheetOpen = true; fabExpanded = false },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (sheetOpen) {
        AddEditTransactionSheet(
            editId = null,
            presetType = presetType,
            defaultCurrency = baseCurrency,
            onDismiss = { sheetOpen = false },
        )
    }
}

@Composable
private fun BalanceCard(balance: Double, currency: Currency, hidden: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.dash_balance),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggle) {
                    Icon(
                        materialIcon(if (hidden) "visibility_off" else "visibility"),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            val animated = animatedCountUp(balance)
            Text(
                text = if (hidden) "••••••" else CurrencyFormatter.format(animated, currency),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun MoneyColumn(label: String, amount: Double, currency: Currency, income: Boolean, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            AmountText(amount, currency, income, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Last7Chart(days: List<DayAmount>) {
    if (days.isEmpty()) return
    val entries = days.mapIndexed { i, d -> entryOf(i.toFloat(), d.expenseBase.toFloat()) }
    val producer = remember(entries) { ChartEntryModelProducer(entries) }
    Chart(
        chart = lineChart(),
        chartModelProducer = producer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = Modifier.fillMaxWidth().height(160.dp),
    )
}

@Composable
private fun Staggered(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { it / 2 }) { content() }
}

@Composable
private fun ExpandableFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onExpense: () -> Unit,
    onIncome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        AnimatedVisibility(expanded) {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.dash_add_income)) },
                    icon = { Icon(materialIcon("add"), null) },
                    onClick = onIncome,
                    containerColor = IncomeGreen,
                    contentColor = Color.White,
                )
                Spacer(Modifier.height(12.dp))
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.dash_add_expense)) },
                    icon = { Icon(materialIcon("add"), null) },
                    onClick = onExpense,
                    containerColor = ExpenseRed,
                    contentColor = Color.White,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        FloatingActionButton(onClick = onToggle) {
            Icon(materialIcon(if (expanded) "close" else "add"), contentDescription = null)
        }
    }
}
