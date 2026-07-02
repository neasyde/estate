package com.financeapp.core.data.mapper

import com.financeapp.core.data.local.entity.BudgetEntity
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.Currency

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    currency = Currency.valueOf(currency),
    period = BudgetPeriod.valueOf(periodType),
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    currency = currency.name,
    periodType = period.name,
)
