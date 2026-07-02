package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>
    suspend fun upsert(r: Reminder): Long
    suspend fun delete(id: Long)
}
