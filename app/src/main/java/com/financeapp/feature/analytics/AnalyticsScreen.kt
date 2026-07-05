package com.financeapp.feature.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.financeapp.core.domain.model.MonthTotals
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
import com.financeapp.core.ui.theme.FrauncesTitle
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.core.utils.DateUtils
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val period by vm.period.collectAsStateWithLifecycle()
    val rolling by vm.rolling.collectAsStateWithLifecycle()
    val currency by vm.baseCurrency.collectAsStateWithLifecycle()

    val hasData = data.slices.isNotEmpty() || data.months.any { it.income > 0 || it.expense > 0 }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.nav_analytics), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        PeriodSelector(period, vm::setPeriod)
        if (period != AnalyticsPeriod.ALL) {
            Spacer(Modifier.height(10.dp))
            RollingToggle(rolling, vm::setRolling)
        }
        Spacer(Modifier.height(20.dp))

        if (!hasData) {
            EmptyState(iconName = "bar_chart", title = stringResource(R.string.an_empty))
        } else {
            Reveal(0) { Summary(data, currency) }
            Spacer(Modifier.height(20.dp)); Hairline(); Spacer(Modifier.height(20.dp))
            Reveal(1) { CategoryBreakdown(data, currency) }
            Spacer(Modifier.height(20.dp)); Hairline(); Spacer(Modifier.height(20.dp))
            Reveal(2) { Trend(data.months) }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun Summary(data: AnalyticsData, currency: Currency) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.dash_income)); Spacer(Modifier.height(4.dp))
                AmountText(data.income, currency, income = true, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            }
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.dash_expense)); Spacer(Modifier.height(4.dp))
                AmountText(data.expense, currency, income = false, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.an_net))
            Spacer(Modifier.weight(1f))
            val net = data.income - data.expense
            Text(
                text = CurrencyFormatter.format(net, currency, LocalShowDecimals.current),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FrauncesTitle, fontSize = 18.sp),
                color = if (net >= 0) IncomeGreen else ExpenseRed,
            )
        }
    }
}

@Composable
private fun CategoryBreakdown(data: AnalyticsData, currency: Currency) {
    val top = data.slices.take(6)
    val rest = data.slices.drop(6)
    val display = if (rest.isNotEmpty()) top + CategorySlice(null, rest.sumOf { it.amount }, rest.sumOf { it.fraction }) else top
    Column {
        Eyebrow(stringResource(R.string.an_by_category))
        Spacer(Modifier.height(8.dp))
        display.forEach { slice -> CategoryBar(slice, currency) }
    }
}

@Composable
private fun CategoryBar(slice: CategorySlice, currency: Currency) {
    Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = slice.category?.let { categoryDisplayName(it) } ?: stringResource(R.string.cat_other),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(88.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.fillMaxWidth(slice.fraction.toFloat().coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = CurrencyFormatter.format(slice.amount, currency, LocalShowDecimals.current),
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FrauncesTitle),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Trend(months: List<MonthTotals>) {
    val peak = months.maxOfOrNull { max(it.income, it.expense) } ?: 0.0
    Column {
        Eyebrow(stringResource(R.string.an_trend))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Dot(IncomeGreen, stringResource(R.string.dash_income)); Spacer(Modifier.width(16.dp)); Dot(ExpenseRed, stringResource(R.string.dash_expense))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().height(150.dp), verticalAlignment = Alignment.Bottom) {
            months.forEach { m ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Bar(m.income, peak, IncomeGreen); Bar(m.expense, peak, ExpenseRed)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(DateUtils.monthLabel(m.monthStart), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Box(Modifier.width(10.dp).height((grow * 112f).dp.coerceAtLeast(if (value > 0) 4.dp else 0.dp)).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(color))
}

@Composable
private fun Dot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(10.dp).height(10.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RollingToggle(rolling: Boolean, onSet: (Boolean) -> Unit) {
    val options = listOf(
        true to stringResource(R.string.an_rolling),
        false to stringResource(R.string.an_calendar),
    )
    Row(Modifier.fillMaxWidth().clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (value, label) ->
            val active = value == rolling
            Box(
                Modifier.weight(1f).clip(PillShape).background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onSet(value) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: AnalyticsPeriod, onSelect: (AnalyticsPeriod) -> Unit) {
    val options = listOf(
        AnalyticsPeriod.WEEK to stringResource(R.string.an_period_week),
        AnalyticsPeriod.MONTH to stringResource(R.string.an_period_month),
        AnalyticsPeriod.YEAR to stringResource(R.string.an_period_year),
        AnalyticsPeriod.ALL to stringResource(R.string.an_period_all),
    )
    Row(Modifier.fillMaxWidth().clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier.weight(1f).clip(PillShape).background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onSelect(value) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

