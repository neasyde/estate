package com.financeapp.core.data.repository

import com.financeapp.core.data.local.dao.BudgetDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> =
        dao.observeAll().map { l -> l.map { it.toDomain() } }

    override suspend fun upsert(b: Budget): Long = dao.upsert(b.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}
