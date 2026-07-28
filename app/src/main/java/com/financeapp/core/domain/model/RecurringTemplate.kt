package com.financeapp.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecurringTemplate(
    val amount: Double,
    val currency: String,
    val type: String,
    val categoryId: Long?,
    val note: String?,
    /**
     * ID of the transaction that originally spawned this rule. Used so editing a transaction
     * updates the existing rule in place (same amount/currency/type) instead of creating a
     * duplicate. `null` for rules created standalone (e.g. from the Recurring screen).
     */
    val linkedTransactionId: Long? = null,
)
