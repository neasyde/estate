package com.financeapp.core.data.mapper

import com.financeapp.core.data.local.entity.TransactionEntity
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.utils.safeValueOf

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    currency = safeValueOf(currency, Currency.RUB),
    type = safeValueOf(type, TransactionType.EXPENSE),
    categoryId = categoryId,
    note = note,
    date = date,
    recurringRuleId = recurringRuleId,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    currency = currency.name,
    type = type.name,
    categoryId = categoryId,
    note = note,
    date = date,
    recurringRuleId = recurringRuleId,
)
