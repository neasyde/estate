package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveManagedCategoriesUseCase @Inject constructor(private val repo: CategoryRepository) {
    operator fun invoke(type: CategoryType): Flow<List<Category>> = repo.observeManagedByType(type)
}

class SaveCategoryUseCase @Inject constructor(private val repo: CategoryRepository) {
    suspend operator fun invoke(c: Category): Long = repo.upsert(c)
}

class DeleteCategoryUseCase @Inject constructor(private val repo: CategoryRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class SetCategoryHiddenUseCase @Inject constructor(private val repo: CategoryRepository) {
    suspend operator fun invoke(id: Long, hidden: Boolean) = repo.setHidden(id, hidden)
}
