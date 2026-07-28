package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.AnalyticsData
import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.CategorySlice
import com.financeapp.core.domain.model.TrendBucket
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.utils.CurrencyConverter
import com.financeapp.core.utils.DateUtils
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

private const val DAY_MS = 86_400_000L

/** Start of the analytics range. Rolling = last N days; calendar = start of this week/month/year. */
fun analyticsStart(now: Long, period: AnalyticsPeriod, rolling: Boolean): Long = when (period) {
    AnalyticsPeriod.WEEK -> if (rolling) now - 7 * DAY_MS else DateUtils.startOfWeek(now)
    AnalyticsPeriod.MONTH -> if (rolling) now - 30 * DAY_MS else DateUtils.startOfMonth(now)
    AnalyticsPeriod.YEAR -> if (rolling) now - 365 * DAY_MS else DateUtils.startOfYear(now)
}

/** The bucket unit the trend chart uses for each period. */
private enum class TrendUnit { DAY, WEEK, MONTH }

/** How many buckets of which unit the trend shows so it matches the selected period's scope. */
private fun trendSpec(period: AnalyticsPeriod): Pair<TrendUnit, Int> = when (period) {
    AnalyticsPeriod.WEEK -> TrendUnit.DAY to 7
    AnalyticsPeriod.MONTH -> TrendUnit.WEEK to 4
    AnalyticsPeriod.YEAR -> TrendUnit.MONTH to 12
}

class GetAnalyticsDataUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    operator fun invoke(now: Long, period: AnalyticsPeriod, rolling: Boolean): Flow<AnalyticsData> =
        combine(txRepo.observeAll(), catRepo.observeAll(), settingsRepo.settings) { txs, cats, s ->
            val byId = cats.associateBy { it.id }
            fun base(t: Transaction) = CurrencyConverter.toBase(t.amount, t.currency, s)

            val start = analyticsStart(now, period, rolling)
            // Upper-bound at the live clock, not the [now] captured when the flow was built, so a
            // transaction added while Analytics is open (dated just past that [now]) is counted at
            // once instead of only after the period is toggled.
            val upper = maxOf(now, System.currentTimeMillis())
            val inPeriod = txs.filter { it.date in start..upper }
            val income = inPeriod.filter { it.type == TransactionType.INCOME }.sumOf { base(it) }
            val expense = inPeriod.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) }

            val slices = inPeriod
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .map { (catId, list) ->
                    val amount = list.sumOf { base(it) }
                    CategorySlice(catId?.let(byId::get), amount, if (expense > 0) amount / expense else 0.0)
                }
                .sortedByDescending { it.amount }

            val (unit, count) = trendSpec(period)
            val zone = java.time.ZoneId.systemDefault()

            val starts: List<Long>
            val ends: List<Long>
            val labels: List<String>

            when {
                // --- WEEK: 7 daily buckets ---
                unit == TrendUnit.DAY -> {
                    val anchors = if (!rolling) {
                        val weekStart = DateUtils.startOfWeek(now)
                        (0 until count).map { i ->
                            Instant.ofEpochMilli(weekStart).atZone(zone).toLocalDate()
                                .plusDays(i.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
                        }
                    } else {
                        DateUtils.lastNDayStarts(now, count)
                    }
                    starts = anchors
                    ends = anchors.map { DateUtils.startOfNextDay(it) }
                    labels = anchors.map { DateUtils.weekdayLabel(it) }
                }
                // --- MONTH: 4 weekly buckets ---
                unit == TrendUnit.WEEK -> {
                    if (!rolling) {
                        // Calendar month: 1–7, 8–14, 15–21, 22–end
                        val monthStart = DateUtils.startOfMonth(now)
                        val monthEnd = DateUtils.startOfNextMonth(now)
                        val firstOfMonth = Instant.ofEpochMilli(monthStart).atZone(zone).toLocalDate()
                        val daysInMonth = firstOfMonth.lengthOfMonth()
                        val ranges = listOf(1..7, 8..14, 15..21, 22..daysInMonth)
                        starts = ranges.map { range ->
                            val day = range.first.coerceAtMost(daysInMonth)
                            firstOfMonth.withDayOfMonth(day).atStartOfDay(zone).toInstant().toEpochMilli()
                        }
                        ends = starts.mapIndexed { index, s ->
                            if (index < starts.lastIndex) starts[index + 1] else monthEnd
                        }
                        labels = starts.mapIndexed { index, s ->
                            val day = Instant.ofEpochMilli(s).atZone(zone).toLocalDate().dayOfMonth
                            val endDay = if (index < starts.lastIndex) {
                                Instant.ofEpochMilli(starts[index + 1]).atZone(zone).toLocalDate().dayOfMonth - 1
                            } else {
                                daysInMonth
                            }
                            "$day-$endDay"
                        }
                    } else {
                        // Rolling month: 4 × 7 days from start
                        starts = (0 until count).map { i ->
                            DateUtils.startOfDay(start + i * 7L * DAY_MS)
                        }
                        ends = starts.mapIndexed { index, s ->
                            if (index < starts.lastIndex) starts[index + 1] else start + 30L * DAY_MS
                        }
                        labels = starts.mapIndexed { index, s ->
                            val startDate = Instant.ofEpochMilli(s).atZone(zone).toLocalDate()
                            val day = startDate.dayOfMonth
                            val endDay = if (index < starts.lastIndex) {
                                val nextDate = Instant.ofEpochMilli(starts[index + 1]).atZone(zone).toLocalDate()
                                if (nextDate.month == startDate.month && nextDate.year == startDate.year) {
                                    nextDate.dayOfMonth - 1
                                } else {
                                    startDate.lengthOfMonth()
                                }
                            } else {
                                Instant.ofEpochMilli(ends[index]).atZone(zone).toLocalDate().dayOfMonth
                            }
                            "$day-$endDay"
                        }
                    }
                }
                // --- YEAR: 12 monthly buckets ---
                unit == TrendUnit.MONTH -> {
                    if (!rolling) {
                        // Calendar year: Jan–Dec of current year
                        val yearStart = DateUtils.startOfYear(now)
                        starts = (0 until count).map { i ->
                            Instant.ofEpochMilli(yearStart).atZone(zone).toLocalDate()
                                .plusMonths(i.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
                        }
                    } else {
                        // Rolling year: last 12 months
                        starts = DateUtils.lastNMonthStarts(now, count)
                    }
                    ends = starts.map { DateUtils.startOfNextMonth(it) }
                    labels = starts.map { DateUtils.monthLabel(it) }
                }
                else -> { starts = emptyList(); ends = emptyList(); labels = emptyList() }
            }

            val trend = starts.mapIndexed { index, bucketStart ->
                val bucketEnd = ends[index]
                val bucketTx = inPeriod.filter { it.date in bucketStart until bucketEnd }
                TrendBucket(
                    start = bucketStart,
                    label = labels[index],
                    income = bucketTx.filter { it.type == TransactionType.INCOME }.sumOf { base(it) },
                    expense = bucketTx.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) },
                )
            }

            AnalyticsData(income, expense, slices, trend)
        }
}
