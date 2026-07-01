package com.financeapp.core.domain.model

data class DayAmount(val dayStart: Long, val expenseBase: Double)

data class DashboardData(
    val balanceBase: Double = 0.0,
    val monthIncomeBase: Double = 0.0,
    val monthExpenseBase: Double = 0.0,
    val last7Days: List<DayAmount> = emptyList(),
    val recent: List<TransactionWithCategory> = emptyList(),
)
