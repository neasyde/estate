package com.financeapp.core.data.repository

import com.financeapp.core.data.local.dao.TransactionDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { l -> l.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        dao.observeRecent(limit).map { l -> l.map { it.toDomain() } }

    override fun observeBetween(start: Long, end: Long): Flow<List<Transaction>> =
        dao.observeBetween(start, end).map { l -> l.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun upsert(t: Transaction): Long = dao.upsert(t.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}
