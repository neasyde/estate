package com.financeapp.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.usecase.ObserveCategoriesUseCase
import com.financeapp.core.domain.usecase.SaveBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BudgetForm(
    val id: Long? = null,
    val categoryId: Long? = null,
    val limit: String = "",
    val currency: Currency = Currency.RUB,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
)

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val save: SaveBudgetUseCase,
    observeCats: ObserveCategoriesUseCase,
) : ViewModel() {
    val expenseCategories: StateFlow<List<Category>> = observeCats(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(BudgetForm())
    val form: StateFlow<BudgetForm> = _form.asStateFlow()

    fun load(initial: Budget?, defaultCurrency: Currency) {
        _form.value = if (initial == null) {
            BudgetForm(currency = defaultCurrency)
        } else {
            BudgetForm(initial.id, initial.categoryId, initial.limitAmount.toString(), initial.currency, initial.period)
        }
    }

    fun setCategory(id: Long) = _form.update { it.copy(categoryId = id) }
    fun setLimit(v: String) = _form.update {
        val filtered = v.filter { c -> c.isDigit() || c == '.' }
        val firstDot = filtered.indexOf('.')
        it.copy(limit = if (firstDot == -1) filtered else filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", ""))
    }
    fun setCurrency(c: Currency) = _form.update { it.copy(currency = c) }
    fun setPeriod(p: BudgetPeriod) = _form.update { it.copy(period = p) }

    suspend fun save(): Boolean {
        val f = _form.value
        val limit = f.limit.toDoubleOrNull()
        if (limit == null || limit <= 0.0 || f.categoryId == null) return false
        save(Budget(f.id ?: 0L, f.categoryId, limit, f.currency, f.period))
        return true
    }
}
