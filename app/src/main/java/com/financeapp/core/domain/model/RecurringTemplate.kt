package com.financeapp.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecurringTemplate(
    val amount: Double,
    val currency: String,
    val type: String,
    val categoryId: Long?,
    val note: String?,
)
