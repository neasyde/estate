package com.financeapp.feature.categories

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.usecase.SaveCategoryUseCase
import com.financeapp.core.ui.theme.CategoryPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CategoryForm(
    val id: Long? = null,
    val name: String = "",
    val icon: String = "more_horiz",
    val color: Int = CategoryPalette[0].toArgb(),
    val type: CategoryType = CategoryType.EXPENSE,
    val iconQuery: String = "",
)

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    private val save: SaveCategoryUseCase,
) : ViewModel() {
    private val _form = MutableStateFlow(CategoryForm())
    val form: StateFlow<CategoryForm> = _form.asStateFlow()

    fun load(initial: Category?, defaultType: CategoryType) {
        _form.value = if (initial == null) {
            CategoryForm(type = defaultType)
        } else {
            CategoryForm(initial.id, initial.name, initial.icon, initial.color, initial.type)
        }
    }

    fun setName(v: String) = _form.update { it.copy(name = v) }
    fun setIcon(v: String) = _form.update { it.copy(icon = v) }
    fun setColor(v: Int) = _form.update { it.copy(color = v) }
    fun setType(v: CategoryType) = _form.update { it.copy(type = v) }
    fun setIconQuery(v: String) = _form.update { it.copy(iconQuery = v) }

    suspend fun save(): Boolean {
        val f = _form.value
        if (f.name.isBlank()) return false
        save(Category(f.id ?: 0L, f.name.trim(), f.icon, f.color, f.type, isCustom = true))
        return true
    }
}
