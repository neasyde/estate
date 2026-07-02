package com.financeapp.core.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Double,
    val currency: Currency,
    val period: BudgetPeriod,
)

data class BudgetProgress(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val fraction: Double,
)
