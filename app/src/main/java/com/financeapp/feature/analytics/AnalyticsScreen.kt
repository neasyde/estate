package com.financeapp.feature.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AnalyticsData
import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.CategorySlice
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.TrendBucket
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.Reveal
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.AmountText
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.theme.LocalBrandType
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.CurrencyFormatter
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val period by vm.period.collectAsStateWithLifecycle()
    val rolling by vm.rolling.collectAsStateWithLifecycle()
    val currency by vm.baseCurrency.collectAsStateWithLifecycle()

    val hasData = data.slices.isNotEmpty() || data.trend.any { it.income > 0 || it.expense > 0 }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.nav_analytics), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodSelector(period, vm::setPeriod, modifier = Modifier.weight(1f))
            RollingToggle(rolling, vm::setRolling, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        if (!hasData) {
            EmptyState(iconName = "bar_chart", title = stringResource(R.string.an_empty))
        } else {
            Reveal(0) { StatsCards(data, currency, period) }
            Spacer(Modifier.height(16.dp))
            Reveal(1) { CategoryBreakdown(data, currency) }
            Spacer(Modifier.height(16.dp))
            Reveal(2) { Trend(data.trend, currency) }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun StatsCards(data: AnalyticsData, currency: Currency, period: AnalyticsPeriod) {
    val net = data.income - data.expense
    val savingsRate = if (data.income > 0) ((data.income - data.expense) / data.income * 100).coerceAtLeast(0.0) else 0.0
    val topCategory = data.slices.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                label = stringResource(R.string.dash_income),
                value = { AmountText(data.income, currency, income = true, style = MaterialTheme.typography.titleMedium) },
                color = IncomeGreen,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.dash_expense),
                value = { AmountText(data.expense, currency, income = false, style = MaterialTheme.typography.titleMedium) },
                color = ExpenseRed,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                label = stringResource(R.string.an_net),
                value = {
                    Text(
                        CurrencyFormatter.format(net, currency, LocalShowDecimals.current),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = LocalBrandType.current.title),
                        color = if (net >= 0) IncomeGreen else ExpenseRed,
                    )
                },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.an_savings_rate),
                value = {
                    Text(
                        "${savingsRate.roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = LocalBrandType.current.title),
                        color = if (savingsRate >= 30) IncomeGreen else MaterialTheme.colorScheme.onSurface,
                    )
                },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: @Composable () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(12.dp),
    ) {
        Eyebrow(label)
        Spacer(Modifier.height(4.dp))
        value()
    }
}

@Composable
private fun MaterialIcon(name: String, tint: Color, modifier: Modifier = Modifier) {
    com.financeapp.core.ui.icons.MaterialIcon(name, tint = tint, modifier = modifier)
}

@Composable
private fun CategoryBreakdown(data: AnalyticsData, currency: Currency) {
    val top = data.slices.take(5)
    val rest = data.slices.drop(5)
    var showAll by remember { mutableStateOf(false) }
    Column {
        Eyebrow(stringResource(R.string.an_by_category))
        Spacer(Modifier.height(8.dp))
        top.forEach { slice -> CategoryBar(slice, currency) }
        AnimatedVisibility(
            visible = showAll,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column { rest.forEach { slice -> CategoryBar(slice, currency) } }
        }
        if (rest.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showAll = !showAll }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (showAll) stringResource(R.string.an_show_less) else stringResource(R.string.an_show_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CategoryBar(slice: CategorySlice, currency: Currency) {
    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = slice.category?.let { categoryDisplayName(it) } ?: stringResource(R.string.cat_other),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(80.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.fillMaxWidth(slice.fraction.toFloat().coerceIn(0f, 1f)).height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = CurrencyFormatter.format(slice.amount, currency, LocalShowDecimals.current),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = LocalBrandType.current.title),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Trend(buckets: List<TrendBucket>, currency: Currency) {
    val peak = buckets.maxOfOrNull { max(it.income, it.expense) } ?: 0.0
    var selectedStart by remember(buckets) { mutableStateOf(buckets.lastOrNull()?.start) }
    val selected = buckets.firstOrNull { it.start == selectedStart } ?: buckets.lastOrNull()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.an_trend))
            Spacer(Modifier.weight(1f))
            Dot(IncomeGreen, stringResource(R.string.dash_income)); Spacer(Modifier.width(10.dp)); Dot(ExpenseRed, stringResource(R.string.dash_expense))
        }
        Spacer(Modifier.height(8.dp))
        if (selected != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    selected.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = LocalBrandType.current.title),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.weight(1f))
                AmountText(selected.income, currency, income = true, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(12.dp))
                AmountText(selected.expense, currency, income = false, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(Modifier.fillMaxWidth().height(120.dp), verticalAlignment = Alignment.Bottom) {
            buckets.forEach { b ->
                val isSelected = selected != null && b.start == selected.start
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
                        .clickable { selectedStart = b.start }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Bar(b.income, peak, IncomeGreen); Bar(b.expense, peak, ExpenseRed)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        b.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun Bar(value: Double, peak: Double, color: Color) {
    val reduced = reducedMotion()
    val target = if (peak > 0) (value / peak).toFloat() else 0f
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }
    val grow by animateFloatAsState(
        targetValue = if (animate) target else 0f,
        animationSpec = if (reduced) tween(0) else tween(Motion.Long, easing = Motion.Emphasized),
        label = "bar",
    )
    Box(Modifier.width(8.dp).height((grow * 88f).dp.coerceAtLeast(if (value > 0) 3.dp else 0.dp)).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(color))
}

@Composable
private fun Dot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(8.dp).height(8.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RollingToggle(rolling: Boolean, onSet: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(
        true to stringResource(R.string.an_rolling),
        false to stringResource(R.string.an_calendar),
    )
    Row(modifier.clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        options.forEach { (value, label) ->
            val active = value == rolling
            Box(
                Modifier.weight(1f).clip(PillShape).background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onSet(value) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: AnalyticsPeriod, onSelect: (AnalyticsPeriod) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(
        AnalyticsPeriod.WEEK to stringResource(R.string.an_period_week),
        AnalyticsPeriod.MONTH to stringResource(R.string.an_period_month),
        AnalyticsPeriod.YEAR to stringResource(R.string.an_period_year),
    )
    Row(modifier.clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier.weight(1f).clip(PillShape).background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onSelect(value) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
