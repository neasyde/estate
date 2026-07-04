package com.financeapp.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.animatedCountUp
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.components.AmountText
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.components.TransactionRow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.FrauncesTitle
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.feature.reminders.AddEditReminderSheet
import com.financeapp.feature.reminders.reminderSubtitle
import com.financeapp.feature.transactions.AddEditTransactionSheet
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onSeeAll: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val data by vm.state.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    val defaultTxType by vm.defaultTxType.collectAsStateWithLifecycle()
    val rates by vm.rates.collectAsStateWithLifecycle()
    val hidden by vm.balanceHidden.collectAsStateWithLifecycle()
    val reminders by vm.upcomingReminders.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var reminderSheetOpen by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Masthead(data.balanceBase, data.monthIncomeBase, data.monthExpenseBase, baseCurrency, hidden, vm::toggleBalanceHidden)

            RatesGlance(rates, baseCurrency)

            Spacer(Modifier.height(28.dp))
            RemindersSection(
                reminders = reminders,
                currency = baseCurrency,
                onAdd = { editingReminder = null; reminderSheetOpen = true },
                onEdit = { editingReminder = it; reminderSheetOpen = true },
            )

            Spacer(Modifier.height(30.dp))
            if (data.recent.isEmpty()) {
                EmptyState(iconName = "wallet", title = stringResource(R.string.dash_empty))
            } else {
                Column {
                    Eyebrow(stringResource(R.string.dash_recent), modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(6.dp))
                    data.recent.forEachIndexed { i, item ->
                        Staggered(i) { TransactionRow(item) }
                        if (i < data.recent.lastIndex) Hairline(Modifier.padding(horizontal = 20.dp))
                    }
                    TextButton(onClick = onSeeAll, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Text(stringResource(R.string.dash_see_all))
                    }
                }
            }
            Spacer(Modifier.height(96.dp))
        }

        FloatingActionButton(
            onClick = { sheetOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        ) {
            Icon(materialIcon("add"), contentDescription = null)
        }
    }

    if (sheetOpen) {
        AddEditTransactionSheet(
            editId = null,
            presetType = defaultTxType,
            defaultCurrency = baseCurrency,
            onDismiss = { sheetOpen = false },
        )
    }

    if (reminderSheetOpen) {
        AddEditReminderSheet(
            initial = editingReminder,
            defaultCurrency = baseCurrency,
            onDismiss = { reminderSheetOpen = false },
            onSave = { vm.saveReminder(it); reminderSheetOpen = false },
        )
    }
}

@Composable
private fun RemindersSection(
    reminders: List<Reminder>,
    currency: Currency,
    onAdd: () -> Unit,
    onEdit: (Reminder) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow(stringResource(R.string.dash_reminders), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onAdd) {
                Icon(materialIcon("add"), contentDescription = null, modifier = Modifier.height(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.rem_add))
            }
        }
        Spacer(Modifier.height(2.dp))
        if (reminders.isEmpty()) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onAdd).padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(materialIcon("notifications"), null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(14.dp))
                Text(
                    stringResource(R.string.dash_reminders_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(materialIcon("chevron_right"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            reminders.forEachIndexed { i, r ->
                ReminderGlance(r, currency, onClick = { onEdit(r) })
                if (i < reminders.lastIndex) Hairline(Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun ReminderGlance(r: Reminder, currency: Currency, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(materialIcon("notifications"), null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(r.title, style = MaterialTheme.typography.bodyLarge)
            Eyebrow(reminderSubtitle(r))
        }
        r.amount?.let {
            Text(
                text = CurrencyFormatter.format(it, r.currency ?: currency),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FrauncesTitle),
            )
        }
    }
}

@Composable
private fun RatesGlance(rates: RatesUi, baseCurrency: Currency) {
    // Rates are RUB per 1 USD/EUR — only meaningful while the base currency is RUB.
    if (baseCurrency != Currency.RUB) return
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        val updated = if (rates.updatedAt <= 0L) {
            stringResource(R.string.dash_rates_never)
        } else {
            val rel = remember(rates.updatedAt) {
                android.text.format.DateUtils.getRelativeTimeSpanString(
                    rates.updatedAt,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS,
                ).toString()
            }
            stringResource(R.string.dash_rates_updated, rel)
        }
        Eyebrow("${stringResource(R.string.dash_rates)} · $updated")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            RateItem("$", rates.usd)
            RateItem("€", rates.eur)
        }
    }
}

@Composable
private fun RateItem(symbol: String, rate: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            symbol,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FrauncesTitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = CurrencyFormatter.format(rate, Currency.RUB),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FrauncesTitle),
            color = MaterialTheme.colorScheme.onBackground,
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
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.dash_balance))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggle) {
                Icon(
                    materialIcon(if (hidden) "visibility_off" else "visibility"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val animated = animatedCountUp(balance)
        Text(
            text = if (hidden) "••••••" else CurrencyFormatter.format(animated, currency, LocalShowDecimals.current),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))
        Hairline()
        Spacer(Modifier.height(16.dp))
        Row {
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.dash_income))
                Spacer(Modifier.height(4.dp))
                AmountText(income, currency, income = true, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            }
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.dash_expense))
                Spacer(Modifier.height(4.dp))
                AmountText(expense, currency, income = false, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            }
        }
    }
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
