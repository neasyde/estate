package com.financeapp.core.domain.model

data class Reminder(
    val id: Long = 0,
    val title: String,
    val amount: Double? = null,
    val currency: Currency? = null,
    val dueDate: Long,
    val notifyDaysBefore: Int = 0,
    val repeat: RepeatType = RepeatType.NONE,
)
