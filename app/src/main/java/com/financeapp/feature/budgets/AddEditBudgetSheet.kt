package com.financeapp.feature.budgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CalculatorSheet
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.accentChipColors
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
    var showCalc by remember { mutableStateOf(false) }

    LaunchedEffect(initial) { vm.load(initial, defaultCurrency) }

    val localeContext = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).verticalScroll(rememberScrollState())) {
            Text(
                stringResource(if (form.id == null) R.string.budget_new else R.string.budget_edit),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.form_category))
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                items(categories, key = { it.id }) { cat ->
                    val sel = form.categoryId == cat.id
                    val interaction = remember { MutableInteractionSource() }
                    val scale by animateFloatAsState(
                        targetValue = if (sel) 1.18f else 1f,
                        animationSpec = if (reducedMotion()) tween(0) else tween(Motion.Short),
                        label = "iconScale",
                    )
                    Column(
                        modifier = Modifier
                            .clickable(interactionSource = interaction, indication = null) { vm.setCategory(cat.id) }
                            .pressScale(interaction)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryIcon(cat, size = 44.dp)
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = { showCalc = true }) {
                        MaterialIcon(symbol("calculate"), contentDescription = null)
                    }
                },
            )
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(Currency.entries) { c ->
                        FilterChip(
                            selected = form.currency == c,
                            onClick = { vm.setCurrency(c) },
                            label = { Text("${c.symbol} ${c.code}") },
                            colors = accentChipColors(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.budget_period))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodChip(stringResource(R.string.budget_period_weekly), form.period == BudgetPeriod.WEEKLY, modifier = Modifier.weight(1f)) { vm.setPeriod(BudgetPeriod.WEEKLY) }
                PeriodChip(stringResource(R.string.budget_period_monthly), form.period == BudgetPeriod.MONTHLY, modifier = Modifier.weight(1f)) { vm.setPeriod(BudgetPeriod.MONTHLY) }
                PeriodChip(stringResource(R.string.budget_period_yearly), form.period == BudgetPeriod.YEARLY, modifier = Modifier.weight(1f)) { vm.setPeriod(BudgetPeriod.YEARLY) }
            }
            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
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
            }
        }
        }
    }

    if (showCalc) {
        CalculatorSheet(
            value = form.limit,
            onValueChange = vm::setLimit,
            currencySymbol = form.currency.symbol,
            onDismiss = { showCalc = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier, colors = accentChipColors())
}
