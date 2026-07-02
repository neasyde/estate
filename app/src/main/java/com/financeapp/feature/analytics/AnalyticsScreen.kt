package com.financeapp.feature.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AnalyticsData
import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.CategorySlice
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.MonthTotals
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.EmptyState
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.components.StatPill
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.utils.CurrencyFormatter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToInt

private val MutedSlice = Color(0xFF9AA0AE)

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val period by vm.period.collectAsStateWithLifecycle()
    val currency by vm.baseCurrency.collectAsStateWithLifecycle()

    val hasData = data.slices.isNotEmpty() || data.months.any { it.income > 0 || it.expense > 0 }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nav_analytics),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))

        PeriodSelector(period, vm::setPeriod)
        Spacer(Modifier.height(16.dp))

        if (!hasData) {
            EmptyState(iconName = "bar_chart", title = stringResource(R.string.an_empty))
        } else {
            Reveal(0) { SummaryCard(data, currency) }
            Spacer(Modifier.height(16.dp))
            Reveal(1) { CategoryCard(data, currency) }
            Spacer(Modifier.height(16.dp))
            Reveal(2) { TrendCard(data.months, currency) }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SummaryCard(data: AnalyticsData, currency: Currency) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(
                icon = "arrow_upward",
                label = stringResource(R.string.dash_income),
                value = CurrencyFormatter.format(data.income, currency),
                contentColor = IncomeGreen,
                containerColor = IncomeGreen.copy(alpha = 0.12f),
                modifier = Modifier.weight(1f),
            )
            StatPill(
                icon = "arrow_downward",
                label = stringResource(R.string.dash_expense),
                value = CurrencyFormatter.format(data.expense, currency),
                contentColor = ExpenseRed,
                containerColor = ExpenseRed.copy(alpha = 0.12f),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.an_net))
            Spacer(Modifier.weight(1f))
            val net = data.income - data.expense
            Text(
                text = CurrencyFormatter.format(net, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (net >= 0) IncomeGreen else ExpenseRed,
            )
        }
    }
}

@Composable
private fun CategoryCard(data: AnalyticsData, currency: Currency) {
    // Keep the donut legible: top 6 slices, remainder folded into one "Other".
    val top = data.slices.take(6)
    val rest = data.slices.drop(6)
    val display = if (rest.isNotEmpty()) {
        top + CategorySlice(null, rest.sumOf { it.amount }, rest.sumOf { it.fraction })
    } else {
        top
    }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
    ) {
        Eyebrow(stringResource(R.string.an_by_category))
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Donut(display, Modifier.size(190.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Eyebrow(stringResource(R.string.an_spent))
                Text(
                    text = CurrencyFormatter.format(data.expense, currency),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        display.forEach { slice ->
            LegendRow(slice, currency)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Donut(slices: List<CategorySlice>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val strokePx = 34.dp.toPx()
        val diameter = size.minDimension - strokePx
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.fraction * 360f).toFloat()
            if (sweep <= 0f) return@forEach
            val gap = if (sweep > 8f) 3f else 0f
            drawArc(
                color = sliceColor(slice),
                startAngle = startAngle + gap / 2f,
                sweepAngle = sweep - gap,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun LegendRow(slice: CategorySlice, currency: Currency) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(sliceColor(slice)))
        Spacer(Modifier.width(12.dp))
        Text(
            text = slice.category?.let { categoryDisplayName(it) } ?: stringResource(R.string.cat_other),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = CurrencyFormatter.format(slice.amount, currency),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "${(slice.fraction * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrendCard(months: List<MonthTotals>, currency: Currency) {
    val peak = months.maxOfOrNull { max(it.income, it.expense) } ?: 0.0
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
    ) {
        Eyebrow(stringResource(R.string.an_trend))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(IncomeGreen, stringResource(R.string.dash_income))
            Spacer(Modifier.width(16.dp))
            LegendDot(ExpenseRed, stringResource(R.string.dash_expense))
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().height(150.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            months.forEach { m ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Bar(m.income, peak, IncomeGreen)
                        Bar(m.expense, peak, ExpenseRed)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = DateUtils.monthLabel(m.monthStart),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val barHeight = (grow * 112f).dp.coerceAtLeast(if (value > 0) 4.dp else 0.dp)
    Box(
        Modifier
            .width(10.dp)
            .height(barHeight)
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(color),
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PeriodSelector(selected: AnalyticsPeriod, onSelect: (AnalyticsPeriod) -> Unit) {
    val options = listOf(
        AnalyticsPeriod.MONTH to stringResource(R.string.an_period_month),
        AnalyticsPeriod.YEAR to stringResource(R.string.an_period_year),
        AnalyticsPeriod.ALL to stringResource(R.string.an_period_all),
    )
    Row(
        Modifier.fillMaxWidth().clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier.weight(1f).clip(PillShape)
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Reveal(index: Int, content: @Composable () -> Unit) {
    val reduced = reducedMotion()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!reduced) delay(index * Motion.StaggerStep.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Medium, easing = Motion.Emphasized)) +
            slideInVertically(tween(Motion.Medium, easing = Motion.Emphasized)) { it / 4 },
    ) { content() }
}

private fun sliceColor(slice: CategorySlice): Color =
    slice.category?.let { Color(it.color) } ?: MutedSlice
