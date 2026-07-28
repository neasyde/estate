package com.financeapp.feature.dashboard

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.ui.anim.Reveal
import com.financeapp.core.ui.anim.animatedCountUp
import com.financeapp.core.ui.components.AmountText
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.components.SwipeTransactionRow
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.LocalBrandType
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
    val reminderDefaults by vm.reminderDefaults.collectAsStateWithLifecycle()
    val hidden by vm.balanceHidden.collectAsStateWithLifecycle()
    val reminders by vm.upcomingReminders.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editTxId by remember { mutableStateOf<Long?>(null) }
    var presetType by remember { mutableStateOf<com.financeapp.core.domain.model.TransactionType?>(null) }
    var reminderSheetOpen by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editTxId = null; sheetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                MaterialIcon(symbol("add"), contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Masthead(data.balanceBase, data.monthIncomeBase, data.monthExpenseBase, baseCurrency, hidden, vm::toggleBalanceHidden)

            // === Single, unified empty state when there's no data yet ===
            // We previously rendered secondary sections (reminders, recent) with their own empty
            // rows, which produced a wall of "Create your first X" hints. Now we collapse the
            // early-stage dashboard into one big "let's get you started" screen and only show
            // the rate glance + reminders when the user has at least one transaction.
            val hasAnyData = data.recent.isNotEmpty() || reminders.isNotEmpty()
            if (!hasAnyData) {
                Spacer(Modifier.height(24.dp))
                FirstRunEmptyState(
                    onAddTransaction = { editTxId = null; sheetOpen = true },
                    onAddReminder = { editingReminder = null; reminderSheetOpen = true },
                )
            } else {
                RatesGlance(rates, baseCurrency)
                Spacer(Modifier.height(28.dp))
                RemindersSection(
                    reminders = reminders,
                    currency = baseCurrency,
                    onAdd = { editingReminder = null; reminderSheetOpen = true },
                    onEdit = { editingReminder = it; reminderSheetOpen = true },
                )
                Spacer(Modifier.height(30.dp))
                RecentSection(
                    recent = data.recent,
                    onSeeAll = onSeeAll,
                    onEdit = { editTxId = it; sheetOpen = true },
                    onDelete = { vm.deleteTransaction(it) },
                    onDuplicate = { vm.duplicateTransaction(it) },
                )
            }
            Spacer(Modifier.height(96.dp))
        }
    }

    if (sheetOpen) {
        AddEditTransactionSheet(
            editId = editTxId,
            presetType = presetType ?: if (editTxId == null) defaultTxType else null,
            defaultCurrency = baseCurrency,
            onDismiss = { sheetOpen = false },
        )
    }

    if (reminderSheetOpen) {
        AddEditReminderSheet(
            initial = editingReminder,
            defaultCurrency = baseCurrency,
            defaultNotifyHour = reminderDefaults.first,
            defaultNotifyDays = reminderDefaults.second,
            onDismiss = { reminderSheetOpen = false },
            onSave = { vm.saveReminder(it); reminderSheetOpen = false },
            onDelete = editingReminder?.let { r -> { vm.deleteReminder(r); reminderSheetOpen = false } },
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
                MaterialIcon(symbol("add"), contentDescription = null, modifier = Modifier.height(18.dp))
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
                MaterialIcon(symbol("notifications"), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(14.dp))
                Text(
                    stringResource(R.string.dash_reminders_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                MaterialIcon(symbol("chevron_right"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        MaterialIcon(symbol("notifications"), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(r.title, style = MaterialTheme.typography.bodyLarge)
            Eyebrow(reminderSubtitle(r))
        }
        r.amount?.let {
            Text(
                text = CurrencyFormatter.format(it, r.currency ?: currency, LocalShowDecimals.current),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = LocalBrandType.current.title),
            )
        }
    }
}

@Composable
private fun RatesGlance(rates: RatesUi, baseCurrency: Currency) {
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

        // Calculate cross-rates based on base currency
        val baseRate = when (baseCurrency) {
            Currency.RUB -> 1.0
            Currency.USD -> rates.usd
            Currency.EUR -> rates.eur
            Currency.CNY -> rates.cny
            Currency.KZT -> rates.kzt
        }

        if (baseRate > 0.0) {
            val rateItems = Currency.entries.filter { it != baseCurrency }.map { c ->
                val rateInRub = when (c) {
                    Currency.USD -> rates.usd
                    Currency.EUR -> rates.eur
                    Currency.CNY -> rates.cny
                    Currency.KZT -> rates.kzt
                    Currency.RUB -> 1.0
                }
                c to (rateInRub / baseRate)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                rateItems.forEach { (currency, rate) ->
                    RateItem(currency.symbol, rate, baseCurrency)
                }
            }
        }
    }
}

@Composable
private fun RateItem(symbol: String, rate: Double, baseCurrency: Currency) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            symbol,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = LocalBrandType.current.title),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = CurrencyFormatter.format(rate, baseCurrency, LocalShowDecimals.current),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = LocalBrandType.current.title),
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
                MaterialIcon(
                    symbol(if (hidden) "visibility_off" else "visibility"),
                    contentDescription = stringResource(R.string.action_toggle_balance),
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

/**
 * First-run dashboard when the user has no transactions and no reminders. Combines what used to be
 * two empty sections into one friendly screen with two clear CTAs.
 */
@Composable
private fun FirstRunEmptyState(
    onAddTransaction: () -> Unit,
    onAddReminder: () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.dash_empty),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            androidx.compose.material3.Button(onClick = onAddTransaction) {
                MaterialIcon(symbol("add"), contentDescription = null, modifier = Modifier.height(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.dash_empty_cta))
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextButton(onClick = onAddReminder) {
                Text(stringResource(R.string.dash_empty_reminder))
            }
        }
    }
}

/**
 * Recently-added transactions. Separated into its own composable so the parent can choose
 * between showing this and the [FirstRunEmptyState].
 */
@Composable
private fun RecentSection(
    recent: List<com.financeapp.core.domain.model.TransactionWithCategory>,
    onSeeAll: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDuplicate: (Long) -> Unit,
) {
    if (recent.isEmpty()) return
    Column {
        Eyebrow(stringResource(R.string.dash_recent), modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(6.dp))
        recent.forEachIndexed { i, item ->
            // Key by transaction id so each row's swipe state follows its transaction
            // when the list mutates (delete/duplicate) instead of being reused by index.
            key(item.transaction.id) {
                Reveal(i) {
                    com.financeapp.core.ui.components.TransactionWithContextMenu(
                        item = item,
                        onEdit = { onEdit(item.transaction.id) },
                        onDelete = { onDelete(item.transaction.id) },
                        onDuplicate = { onDuplicate(item.transaction.id) },
                    ) {
                        SwipeTransactionRow(
                            item = item,
                            onDelete = { onDelete(item.transaction.id) },
                            onDuplicate = { onDuplicate(item.transaction.id) },
                            onClick = { onEdit(item.transaction.id) },
                        )
                    }
                }
            }
        }
        TextButton(onClick = onSeeAll, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text(stringResource(R.string.dash_see_all))
        }
    }
}

