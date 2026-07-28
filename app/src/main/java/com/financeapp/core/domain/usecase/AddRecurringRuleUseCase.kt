package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.repository.RecurringRuleRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Adds a recurring rule, or updates the existing one when the same source transaction already
 * has a rule. This is what makes "edit a transaction that was already recurring" idempotent —
 * the user changes the category / amount / interval and the rule follows instead of piling up.
 */
class AddRecurringRuleUseCase @Inject constructor(
    private val repo: RecurringRuleRepository,
    @javax.inject.Named("default") private val json: Json,
) {
    suspend operator fun invoke(
        template: RecurringTemplate,
        interval: IntervalType,
        startDate: Long,
        autoAdd: Boolean,
    ) {
        val serialized = json.encodeToString(template)
        val next = nextDate(startDate, interval)

        val existing = template.linkedTransactionId
            ?.let { repo.findByLinkedTransactionId(it) }
        if (existing != null) {
            repo.update(
                id = existing.id,
                templateJson = serialized,
                interval = interval,
                nextDate = next,
                autoAdd = autoAdd,
            )
        } else {
            repo.add(serialized, interval, next, autoAdd)
        }
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
