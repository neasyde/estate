package com.financeapp.feature.onboarding

import androidx.lifecycle.ViewModel
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.SetPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnbState(
    val language: AppLanguage = AppLanguage.RU,
    val baseCurrency: Currency = Currency.RUB,
    val rateUsd: String = "90",
    val rateEur: String = "100",
    val pin: String = "",
    val biometric: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val setPinUseCase: SetPinUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(OnbState())
    val state: StateFlow<OnbState> = _state.asStateFlow()

    fun setLanguage(l: AppLanguage) = _state.update { it.copy(language = l) }
    fun setCurrency(c: Currency) = _state.update { it.copy(baseCurrency = c) }
    fun setRateUsd(v: String) = _state.update { it.copy(rateUsd = v) }
    fun setRateEur(v: String) = _state.update { it.copy(rateEur = v) }
    fun setPin(v: String) = _state.update { it.copy(pin = v.filter(Char::isDigit).take(4)) }
    fun toggleBiometric(b: Boolean) = _state.update { it.copy(biometric = b) }

    suspend fun finish() {
        val s = _state.value
        settingsRepo.setLanguage(s.language)
        settingsRepo.setBaseCurrency(s.baseCurrency)
        settingsRepo.setRates(s.rateUsd.toDoubleOrNull() ?: 90.0, s.rateEur.toDoubleOrNull() ?: 100.0)
        settingsRepo.setBiometricEnabled(s.biometric)
        if (s.pin.length == 4) setPinUseCase(s.pin)
        settingsRepo.setOnboardingCompleted(true)
    }
}
