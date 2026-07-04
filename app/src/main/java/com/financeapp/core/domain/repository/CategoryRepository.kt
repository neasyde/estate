package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeByType(type: CategoryType): Flow<List<Category>>
    fun observeManagedByType(type: CategoryType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun upsert(c: Category): Long
    suspend fun setHidden(id: Long, hidden: Boolean)
    suspend fun reorder(orderedIds: List<Long>)
    suspend fun delete(id: Long)
}
