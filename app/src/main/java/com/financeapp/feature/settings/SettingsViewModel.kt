package com.financeapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.SetPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val setPin: SetPinUseCase,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTheme(m: ThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(m) }
    fun setScheme(s: ColorScheme) = viewModelScope.launch { settingsRepo.setColorScheme(s) }
    fun setLanguage(l: AppLanguage) = viewModelScope.launch { settingsRepo.setLanguage(l) }
    fun setBaseCurrency(c: Currency) = viewModelScope.launch { settingsRepo.setBaseCurrency(c) }
    fun setRates(usd: Double, eur: Double) = viewModelScope.launch { settingsRepo.setRates(usd, eur) }
    fun setBiometric(b: Boolean) = viewModelScope.launch { settingsRepo.setBiometricEnabled(b) }
    fun changePin(pin: String) = viewModelScope.launch { setPin(pin) }
}
