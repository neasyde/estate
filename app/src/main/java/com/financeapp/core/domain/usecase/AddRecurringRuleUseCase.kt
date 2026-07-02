package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.repository.RecurringRuleRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class AddRecurringRuleUseCase @Inject constructor(
    private val repo: RecurringRuleRepository,
) {
    suspend operator fun invoke(
        template: RecurringTemplate,
        interval: IntervalType,
        startDate: Long,
        autoAdd: Boolean,
    ): Long {
        val json = Json.encodeToString(template)
        return repo.add(json, interval, nextDate(startDate, interval), autoAdd)
    }

    private fun nextDate(from: Long, interval: IntervalType): Long {
        val d = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault())
        val n = when (interval) {
            IntervalType.DAILY -> d.plusDays(1)
            IntervalType.WEEKLY -> d.plusWeeks(1)
            IntervalType.MONTHLY -> d.plusMonths(1)
            IntervalType.YEARLY -> d.plusYears(1)
        }
        return n.toInstant().toEpochMilli()
    }
}
