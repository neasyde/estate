package com.financeapp.feature.budgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.BudgetProgress
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.BudgetAmber
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.CurrencyFormatter
import kotlin.math.roundToInt

@Composable
fun BudgetsScreen(vm: BudgetsViewModel = hiltViewModel()) {
    val budgets by vm.budgets.collectAsStateWithLifecycle()
    val base by vm.baseCurrency.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Budget?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; sheetOpen = true }) {
                Icon(materialIcon("add"), contentDescription = null)
            }
        },
    ) { padding ->
        if (budgets.isEmpty()) {
            EmptyState(
                iconName = "savings",
                title = stringResource(R.string.ph_budgets),
                ctaText = stringResource(R.string.budget_new),
                onCta = { editing = null; sheetOpen = true },
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
                items(budgets, key = { it.budget.id }) { bp ->
                    BudgetCard(bp, onEdit = { editing = bp.budget; sheetOpen = true }, onDelete = { vm.delete(bp.budget.id) })
                }
            }
        }
    }

    if (sheetOpen) {
        AddEditBudgetSheet(initial = editing, defaultCurrency = base, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun BudgetCard(bp: BudgetProgress, onEdit: () -> Unit, onDelete: () -> Unit) {
    val over = bp.fraction >= 1.0
    val barColor = when {
        bp.fraction >= 1.0 -> ExpenseRed
        bp.fraction >= 0.8 -> BudgetAmber
        else -> IncomeGreen
    }
    SoftCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onEdit,
        border = true,
        containerColor = MaterialTheme.colorScheme.background,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryIcon(bp.category)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = bp.category?.let { categoryDisplayName(it) } ?: stringResource(R.string.cat_other),
                    style = MaterialTheme.typography.titleMedium,
                )
                Eyebrow(periodLabel(bp.budget.period))
            }
            Box(
                Modifier.clip(PillShape).background(barColor.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${(bp.fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(materialIcon("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        ProgressBar(bp.fraction, barColor)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(CurrencyFormatter.format(bp.spent, bp.budget.currency), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            Text(
                CurrencyFormatter.format(bp.budget.limitAmount, bp.budget.currency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (over) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(materialIcon("warning"), null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.budget_over), style = MaterialTheme.typography.labelMedium, color = ExpenseRed)
            }
        }
    }
}

@Composable
private fun ProgressBar(fraction: Double, color: Color) {
    val reduced = reducedMotion()
    val target = fraction.coerceIn(0.0, 1.0).toFloat()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target, reduced) {
        if (reduced) anim.snapTo(target)
        else anim.animateTo(target, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
    }
    Box(
        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            Modifier.fillMaxWidth(anim.value).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color),
        )
    }
}

@Composable
private fun periodLabel(period: BudgetPeriod): String = stringResource(
    when (period) {
        BudgetPeriod.WEEKLY -> R.string.budget_period_weekly
        BudgetPeriod.MONTHLY -> R.string.budget_period_monthly
        BudgetPeriod.YEARLY -> R.string.budget_period_yearly
    },
)
