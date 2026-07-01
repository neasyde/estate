package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeBetween(start: Long, end: Long): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun upsert(t: Transaction): Long
    suspend fun delete(id: Long)
}
