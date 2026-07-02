package com.financeapp.core.data.repository

import com.financeapp.core.data.local.dao.ReminderDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {
    override fun observeAll(): Flow<List<Reminder>> =
        dao.observeAll().map { l -> l.map { it.toDomain() } }

    override suspend fun upsert(r: Reminder): Long = dao.upsert(r.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}
