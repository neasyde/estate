package com.financeapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DashboardData
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.GetDashboardDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboard: GetDashboardDataUseCase,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<DashboardData> = getDashboard(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    private val _balanceHidden = MutableStateFlow(false)
    val balanceHidden: StateFlow<Boolean> = _balanceHidden.asStateFlow()

    fun toggleBalanceHidden() = _balanceHidden.update { !it }
}
