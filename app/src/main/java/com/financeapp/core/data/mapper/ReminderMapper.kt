package com.financeapp.core.data.mapper

import com.financeapp.core.data.local.entity.ReminderEntity
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.financeapp.core.utils.safeValueOf

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    title = title,
    amount = amount,
    currency = currency?.let { safeValueOf(it, Currency.RUB) },
    dueDate = dueDate,
    notifyDaysBefore = notifyDaysBefore,
    repeat = safeValueOf(repeatType, RepeatType.NONE),
    notifyMinuteOfDay = notifyMinuteOfDay,
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    title = title,
    amount = amount,
    currency = currency?.name,
    dueDate = dueDate,
    notifyDaysBefore = notifyDaysBefore,
    repeatType = repeat.name,
    notifyMinuteOfDay = notifyMinuteOfDay,
)
