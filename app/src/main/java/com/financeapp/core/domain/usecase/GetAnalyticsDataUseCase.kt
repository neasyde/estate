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
    AnalyticsPeriod.MONTH -> TrendUnit.WEEK to 5
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
            val starts = when (unit) {
                TrendUnit.DAY -> DateUtils.lastNDayStarts(now, count)
                TrendUnit.WEEK -> DateUtils.lastNWeekStarts(now, count)
                TrendUnit.MONTH -> DateUtils.lastNMonthStarts(now, count)
            }
            val trend = starts.map { bucketStart ->
                val next = when (unit) {
                    TrendUnit.DAY -> DateUtils.startOfNextDay(bucketStart)
                    TrendUnit.WEEK -> DateUtils.startOfNextWeek(bucketStart)
                    TrendUnit.MONTH -> DateUtils.startOfNextMonth(bucketStart)
                }
                val bucketTx = txs.filter { it.date in bucketStart until next }
                TrendBucket(
                    start = bucketStart,
                    label = when (unit) {
                        TrendUnit.DAY -> DateUtils.weekdayLabel(bucketStart)
                        TrendUnit.WEEK -> DateUtils.dayOfMonthLabel(bucketStart)
                        TrendUnit.MONTH -> DateUtils.monthLabel(bucketStart)
                    },
                    income = bucketTx.filter { it.type == TransactionType.INCOME }.sumOf { base(it) },
                    expense = bucketTx.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) },
                )
            }

            AnalyticsData(income, expense, slices, trend)
        }
}
