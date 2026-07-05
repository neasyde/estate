package com.financeapp.core.domain.model

/** Range the category breakdown and totals are computed over. */
enum class AnalyticsPeriod { WEEK, MONTH, YEAR, ALL }

/** One category's share of expenses in the selected period. */
data class CategorySlice(
    val category: Category?,
    val amount: Double,
    val fraction: Double,
)

/** Income and expense totals for a single calendar month. */
data class MonthTotals(
    val monthStart: Long,
    val income: Double,
    val expense: Double,
)

data class AnalyticsData(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val slices: List<CategorySlice> = emptyList(),
    val months: List<MonthTotals> = emptyList(),
)
