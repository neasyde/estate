package com.financeapp.core.domain.model

data class Reminder(
    val id: Long = 0,
    val title: String,
    val amount: Double? = null,
    val currency: Currency? = null,
    val dueDate: Long,
    val notifyDaysBefore: Int = 0,
    val repeat: RepeatType = RepeatType.NONE,
    /** Time of day the notification fires, as minutes from midnight (default 09:00 = 540). */
    val notifyMinuteOfDay: Int = 9 * 60,
)
