package com.financeapp.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.BudgetProgress
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.DeleteBudgetUseCase
import com.financeapp.core.domain.usecase.ObserveBudgetsWithSpentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    observeBudgets: ObserveBudgetsWithSpentUseCase,
    settingsRepo: SettingsRepository,
    private val deleteBudget: DeleteBudgetUseCase,
) : ViewModel() {
    val budgets: StateFlow<List<BudgetProgress>> = observeBudgets(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    fun delete(id: Long) = viewModelScope.launch { deleteBudget(id) }
}
