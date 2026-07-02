package com.financeapp.core.data.repository

import com.financeapp.core.data.local.dao.RecurringRuleDao
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.repository.RecurringRuleRepository
import javax.inject.Inject

class RecurringRuleRepositoryImpl @Inject constructor(
    private val dao: RecurringRuleDao,
) : RecurringRuleRepository {
    override suspend fun add(
        templateJson: String,
        interval: IntervalType,
        nextDate: Long,
        autoAdd: Boolean,
    ): Long = dao.upsert(
        RecurringRuleEntity(
            templateJson = templateJson,
            intervalType = interval.name,
            nextDate = nextDate,
            autoAdd = autoAdd,
        ),
    )
}
