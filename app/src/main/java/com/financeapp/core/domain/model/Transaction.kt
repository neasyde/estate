package com.financeapp.core.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val currency: Currency,
    val type: TransactionType,
    val categoryId: Long?,
    val note: String?,
    val date: Long,
    val recurringRuleId: Long? = null,
)
