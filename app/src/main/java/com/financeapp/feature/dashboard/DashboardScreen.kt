package com.financeapp.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DayAmount
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.animatedCountUp
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.components.StatPill
import com.financeapp.core.ui.components.TransactionRow
import com.financeapp.core.ui.components.accentBrush
import com.financeapp.core.ui.components.softShadow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.theme.LocalAccentColors
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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Masthead(data.balanceBase, data.monthIncomeBase, data.monthExpenseBase, baseCurrency, hidden, vm::toggleBalanceHidden)

            Spacer(Modifier.height(16.dp))
            SectionCard {
                Eyebrow(stringResource(R.string.dash_last7), modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                Last7Chart(data.last7Days, Modifier.padding(horizontal = 16.dp))
            }

            Spacer(Modifier.height(16.dp))
            if (data.recent.isEmpty()) {
                EmptyState(iconName = "wallet", title = stringResource(R.string.dash_empty))
            } else {
                SectionCard {
                    Eyebrow(stringResource(R.string.dash_recent), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))
                    data.recent.forEachIndexed { i, item ->
                        Staggered(i) { TransactionRow(item) }
                    }
                    TextButton(onClick = onSeeAll, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(stringResource(R.string.dash_see_all))
                    }
                }
            }
            Spacer(Modifier.height(96.dp))
        }

        ExpandableFab(
            expanded = fabExpanded,
            onToggle = { fabExpanded = !fabExpanded },
            onExpense = { presetType = TransactionType.EXPENSE; sheetOpen = true; fabExpanded = false },
            onIncome = { presetType = TransactionType.INCOME; sheetOpen = true; fabExpanded = false },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
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
private fun Masthead(
    balance: Double,
    income: Double,
    expense: Double,
    currency: Currency,
    hidden: Boolean,
    onToggle: () -> Unit,
) {
    val accent = LocalAccentColors.current.primary
    val onHero = Color.White
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .softShadow(MaterialTheme.shapes.extraLarge, accent, 20.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(accentBrush())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.dash_balance), color = onHero.copy(alpha = 0.75f))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggle) {
                Icon(
                    materialIcon(if (hidden) "visibility_off" else "visibility"),
                    contentDescription = null,
                    tint = onHero,
                )
            }
        }
        val animated = animatedCountUp(balance)
        Text(
            text = if (hidden) "••••••" else CurrencyFormatter.format(animated, currency),
            style = MaterialTheme.typography.displayLarge,
            color = onHero,
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(
                icon = "arrow_upward",
                label = stringResource(R.string.dash_income),
                value = CurrencyFormatter.format(income, currency),
                contentColor = onHero,
                containerColor = onHero.copy(alpha = 0.16f),
                modifier = Modifier.weight(1f),
            )
            StatPill(
                icon = "arrow_downward",
                label = stringResource(R.string.dash_expense),
                value = CurrencyFormatter.format(expense, currency),
                contentColor = onHero,
                containerColor = onHero.copy(alpha = 0.16f),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = 8.dp,
        content = content,
    )
}

@Composable
private fun Last7Chart(days: List<DayAmount>, modifier: Modifier = Modifier) {
    if (days.isEmpty()) return
    val entries = days.mapIndexed { i, d -> entryOf(i.toFloat(), d.expenseBase.toFloat()) }
    val producer = remember(entries) { ChartEntryModelProducer(entries) }
    Chart(
        chart = lineChart(),
        chartModelProducer = producer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = modifier.fillMaxWidth().height(150.dp),
    )
}

@Composable
private fun Staggered(index: Int, content: @Composable () -> Unit) {
    val reduced = reducedMotion()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!reduced) delay(index * Motion.StaggerStep.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Medium, easing = Motion.Emphasized)) +
            slideInVertically(tween(Motion.Medium, easing = Motion.Emphasized)) { it / 3 },
    ) { content() }
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
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(Motion.Short, easing = Motion.Emphasized)) +
                slideInVertically(tween(Motion.Short, easing = Motion.Emphasized)) { it / 2 },
        ) {
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
