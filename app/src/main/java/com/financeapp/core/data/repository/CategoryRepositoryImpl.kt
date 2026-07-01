package com.financeapp.core.data.repository

import com.financeapp.core.data.local.dao.CategoryDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { l -> l.map { it.toDomain() } }

    override fun observeByType(type: CategoryType): Flow<List<Category>> =
        dao.observeByType(type.name).map { l -> l.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()

    override suspend fun upsert(c: Category): Long = dao.upsert(c.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}
