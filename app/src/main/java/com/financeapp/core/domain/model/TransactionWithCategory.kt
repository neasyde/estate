package com.financeapp.core.domain.model

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?,
)
