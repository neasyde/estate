package com.financeapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double?,
    val currency: String?,
    val dueDate: Long,
    val notifyDaysBefore: Int,
    val repeatType: String,
)
