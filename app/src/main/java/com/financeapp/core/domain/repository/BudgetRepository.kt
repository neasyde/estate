package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    suspend fun upsert(b: Budget): Long
    suspend fun delete(id: Long)
}
