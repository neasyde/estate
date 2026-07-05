package com.financeapp.core.domain.model

/** Range the category breakdown and totals are computed over. */
enum class AnalyticsPeriod { WEEK, MONTH, YEAR, ALL }

/** One category's share of expenses in the selected period. */
data class CategorySlice(
    val category: Category?,
    val amount: Double,
    val fraction: Double,
)

/**
 * Income and expense totals for one bucket of the trend chart. The bucket unit adapts to the
 * selected period (a day for Week, a week for Month, a month for Year/All) and [label] is the
 * pre-formatted axis caption for that unit.
 */
data class TrendBucket(
    val start: Long,
    val label: String,
    val income: Double,
    val expense: Double,
)

data class AnalyticsData(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val slices: List<CategorySlice> = emptyList(),
    val trend: List<TrendBucket> = emptyList(),
)
