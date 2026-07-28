package com.financeapp.core.domain.repository

import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.domain.model.IntervalType

interface RecurringRuleRepository {
    suspend fun add(templateJson: String, interval: IntervalType, nextDate: Long, autoAdd: Boolean): Long

    /**
     * Find a recurring rule whose template references [linkedTransactionId], or null if none exists.
     */
    suspend fun findByLinkedTransactionId(linkedTransactionId: Long): RecurringRuleEntity?

    /**
     * Update an existing rule with new template/interval/nextDate/autoAdd. The id is preserved.
     */
    suspend fun update(
        id: Long,
        templateJson: String,
        interval: IntervalType,
        nextDate: Long,
        autoAdd: Boolean,
    )
}
