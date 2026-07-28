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

    override suspend fun findByLinkedTransactionId(linkedTransactionId: Long): RecurringRuleEntity? =
        dao.findByLinkedTransactionId(
            pattern1 = "%\"linkedTransactionId\":$linkedTransactionId,%",
            pattern2 = "%\"linkedTransactionId\":$linkedTransactionId}%",
        )

    override suspend fun update(
        id: Long,
        templateJson: String,
        interval: IntervalType,
        nextDate: Long,
        autoAdd: Boolean,
    ) {
        // Preserve the existing row's `enabled` flag and any future fields by round-tripping.
        val current = dao.getById(id) ?: return
        dao.upsert(
            current.copy(
                templateJson = templateJson,
                intervalType = interval.name,
                nextDate = nextDate,
                autoAdd = autoAdd,
            ),
        )
    }
}
