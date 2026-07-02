package com.financeapp.feature.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetSheet(
    initial: Budget?,
    defaultCurrency: Currency,
    onDismiss: () -> Unit,
    vm: AddEditBudgetViewModel = hiltViewModel(),
) {
    val form by vm.form.collectAsStateWithLifecycle()
    val categories by vm.expenseCategories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initial) { vm.load(initial, defaultCurrency) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(
                stringResource(if (form.id == null) R.string.budget_new else R.string.budget_edit),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.form_category))
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories) { cat ->
                    val sel = form.categoryId == cat.id
                    Column(
                        modifier = Modifier.clickable { vm.setCategory(cat.id) }.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryIcon(cat, size = if (sel) 52.dp else 44.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            categoryDisplayName(cat),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = form.limit,
                onValueChange = vm::setLimit,
                label = { Text(stringResource(R.string.budget_limit)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.set_base_currency))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { c ->
                    FilterChip(form.currency == c, { vm.setCurrency(c) }, { Text("${c.symbol} ${c.code}") })
                }
            }
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.budget_period))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodChip(stringResource(R.string.budget_period_weekly), form.period == BudgetPeriod.WEEKLY) { vm.setPeriod(BudgetPeriod.WEEKLY) }
                PeriodChip(stringResource(R.string.budget_period_monthly), form.period == BudgetPeriod.MONTHLY) { vm.setPeriod(BudgetPeriod.MONTHLY) }
                PeriodChip(stringResource(R.string.budget_period_yearly), form.period == BudgetPeriod.YEARLY) { vm.setPeriod(BudgetPeriod.YEARLY) }
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
