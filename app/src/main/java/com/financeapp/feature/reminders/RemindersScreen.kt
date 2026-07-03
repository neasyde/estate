package com.financeapp.feature.reminders

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.FrauncesTitle
import com.financeapp.core.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit, vm: RemindersViewModel = hiltViewModel()) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

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
                itemsIndexed(reminders, key = { _, r -> r.id }) { i, r ->
                    ReminderCard(r, onEdit = { editing = r; sheetOpen = true }, onDelete = { vm.delete(r) })
                    if (i < reminders.lastIndex) Hairline(Modifier.padding(horizontal = 20.dp))
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
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(materialIcon("notifications"), null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(r.title, style = MaterialTheme.typography.titleMedium)
            Eyebrow(reminderSubtitle(r))
        }
        r.amount?.let {
            Text(
                text = CurrencyFormatter.format(it, r.currency ?: Currency.RUB),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FrauncesTitle),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(materialIcon("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
