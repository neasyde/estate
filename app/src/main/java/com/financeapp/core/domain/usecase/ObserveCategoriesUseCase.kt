package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val repo: CategoryRepository,
) {
    operator fun invoke(type: CategoryType? = null): Flow<List<Category>> =
        if (type == null) repo.observeAll() else repo.observeByType(type)
}
