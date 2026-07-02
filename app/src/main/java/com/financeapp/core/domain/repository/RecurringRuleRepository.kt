package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.IntervalType

interface RecurringRuleRepository {
    suspend fun add(templateJson: String, interval: IntervalType, nextDate: Long, autoAdd: Boolean): Long
}
