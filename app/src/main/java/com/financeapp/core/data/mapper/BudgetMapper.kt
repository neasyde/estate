package com.financeapp.core.data.mapper

import com.financeapp.core.data.local.entity.BudgetEntity
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.utils.safeValueOf

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    currency = safeValueOf(currency, Currency.RUB),
    period = safeValueOf(periodType, BudgetPeriod.MONTHLY),
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    currency = currency.name,
    periodType = period.name,
)
