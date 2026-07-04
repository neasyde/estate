package com.financeapp.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.usecase.DeleteCategoryUseCase
import com.financeapp.core.domain.usecase.ObserveManagedCategoriesUseCase
import com.financeapp.core.domain.usecase.SetCategoryHiddenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeManaged: ObserveManagedCategoriesUseCase,
    private val setHidden: SetCategoryHiddenUseCase,
    private val deleteCat: DeleteCategoryUseCase,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {
    private val _tab = MutableStateFlow(CategoryType.EXPENSE)
    val tab: StateFlow<CategoryType> = _tab.asStateFlow()

    val categories: StateFlow<List<Category>> = _tab
        .flatMapLatest { observeManaged(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(t: CategoryType) { _tab.value = t }
    fun toggleHidden(c: Category) = viewModelScope.launch { setHidden(c.id, !c.isHidden) }
    fun delete(id: Long) = viewModelScope.launch { deleteCat(id) }
    fun reorder(ids: List<Long>) = viewModelScope.launch { categoryRepo.reorder(ids) }
}
