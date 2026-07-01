package com.financeapp.core.domain.model

data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val currency: Currency? = null,
    val start: Long? = null,
    val end: Long? = null,
    val query: String = "",
)
