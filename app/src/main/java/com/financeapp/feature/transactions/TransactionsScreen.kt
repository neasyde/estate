package com.financeapp.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.TransactionRow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.utils.DateUtils
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(vm: TransactionsViewModel = hiltViewModel()) {
    val grouped by vm.grouped.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val now = remember { System.currentTimeMillis() }

    var sheetOpen by remember { mutableStateOf(false) }
    var editId by remember { mutableStateOf<Long?>(null) }

    val deletedMsg = stringResource(R.string.tx_deleted)
    val copiedMsg = stringResource(R.string.tx_copied)
    val undoLabel = stringResource(R.string.action_undo)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editId = null; sheetOpen = true }) {
                Icon(materialIcon("add"), contentDescription = null)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(stringResource(R.string.tx_search)) },
                leadingIcon = { Icon(materialIcon("search"), null) },
                singleLine = true,
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(filter.type == null, { vm.setTypeFilter(null) }, { Text(stringResource(R.string.tx_filter_all)) })
                FilterChip(filter.type == TransactionType.INCOME, { vm.setTypeFilter(TransactionType.INCOME) }, { Text(stringResource(R.string.tx_filter_income)) })
                FilterChip(filter.type == TransactionType.EXPENSE, { vm.setTypeFilter(TransactionType.EXPENSE) }, { Text(stringResource(R.string.tx_filter_expense)) })
            }
            Spacer(Modifier.padding(4.dp))

            if (grouped.isEmpty()) {
                EmptyState(
                    iconName = "receipt_long",
                    title = stringResource(R.string.tx_empty),
                    ctaText = stringResource(R.string.dash_add_expense),
                    onCta = { editId = null; sheetOpen = true },
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    grouped.forEach { (day, items) ->
                        item(key = "header_$day") { DayHeader(day, now) }
                        items(items, key = { it.transaction.id }) { item ->
                            SwipeRow(
                                item = item,
                                onDelete = {
                                    vm.delete(item.transaction)
                                    scope.launch {
                                        val r = snackbarHost.showSnackbar(deletedMsg, undoLabel)
                                        if (r == SnackbarResult.ActionPerformed) vm.undoDelete()
                                    }
                                },
                                onDuplicate = {
                                    haptics(true)
                                    vm.duplicate(item.transaction.id)
                                    scope.launch { snackbarHost.showSnackbar(copiedMsg) }
                                },
                                onClick = { editId = item.transaction.id; sheetOpen = true },
                            )
                        }
                    }
                }
            }
        }
    }

    if (sheetOpen) {
        AddEditTransactionSheet(
            editId = editId,
            presetType = null,
            defaultCurrency = baseCurrency,
            onDismiss = { sheetOpen = false },
        )
    }
}

@Composable
private fun DayHeader(dayStart: Long, now: Long) {
    val label = when (val l = DateUtils.dayLabel(dayStart, now)) {
        DateUtils.DayLabel.Today -> stringResource(R.string.tx_today)
        DateUtils.DayLabel.Yesterday -> stringResource(R.string.tx_yesterday)
        is DateUtils.DayLabel.Other -> l.text
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRow(
    item: TransactionWithCategory,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); false }
                SwipeToDismissBoxValue.StartToEnd -> { onDuplicate(); false }
                else -> false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> ExpenseRed
                SwipeToDismissBoxValue.StartToEnd -> IncomeGreen
                else -> Color.Transparent
            }
            val align = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) "delete" else "content_copy"
            Box(
                Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium).background(color).padding(horizontal = 24.dp),
                contentAlignment = align,
            ) {
                Icon(materialIcon(icon), contentDescription = null, tint = Color.White)
            }
        },
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
        ) {
            TransactionRow(item = item, onClick = onClick)
        }
    }
}
