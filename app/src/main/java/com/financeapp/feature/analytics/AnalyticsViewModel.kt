package com.financeapp.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.AnalyticsData
import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.GetAnalyticsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    getAnalytics: GetAnalyticsDataUseCase,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    private val _period = MutableStateFlow(AnalyticsPeriod.MONTH)
    val period: StateFlow<AnalyticsPeriod> = _period.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: StateFlow<AnalyticsData> = _period
        .flatMapLatest { getAnalytics(System.currentTimeMillis(), it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsData())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    fun setPeriod(p: AnalyticsPeriod) = _period.update { p }
}
