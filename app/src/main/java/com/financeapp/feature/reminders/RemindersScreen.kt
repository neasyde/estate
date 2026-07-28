package com.financeapp.feature.reminders

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.LocalBrandType
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit, vm: RemindersViewModel = hiltViewModel()) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    val reminderDefaults by vm.reminderDefaults.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rem_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { MaterialIcon(symbol("arrow_back"), contentDescription = stringResource(R.string.action_back)) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; sheetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                MaterialIcon(symbol("add"), contentDescription = stringResource(R.string.action_add))
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
                itemsIndexed(reminders, key = { _, r -> r.id }) { _, r ->
                    ReminderCard(r, onEdit = { editing = r; sheetOpen = true }, onDelete = { vm.delete(r) })
                }
            }
        }
    }

    if (sheetOpen) {
        AddEditReminderSheet(
            initial = editing,
            defaultCurrency = baseCurrency,
            defaultNotifyHour = reminderDefaults.first,
            defaultNotifyDays = reminderDefaults.second,
            onDismiss = { sheetOpen = false },
            onSave = { vm.save(it); sheetOpen = false },
            onDelete = editing?.let { r -> { vm.delete(r); sheetOpen = false } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(r: Reminder, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .pressScale(interaction),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MaterialIcon(symbol("notifications"), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.fillMaxWidth(0.7f)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.padding(2.dp))
                Eyebrow(reminderSubtitle(r))
            }
            r.amount?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = CurrencyFormatter.format(it, r.currency ?: Currency.RUB, LocalShowDecimals.current),
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = LocalBrandType.current.title),
                )
            }
            IconButton(onClick = onDelete) {
                MaterialIcon(symbol("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
