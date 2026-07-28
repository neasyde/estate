package com.financeapp.feature.onboarding

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.config.AppConfig
import com.financeapp.core.data.remote.ExchangeRateApi
import com.financeapp.core.data.remote.ExchangeResult
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.SetPinUseCase
import com.financeapp.core.domain.usecase.resolveApiKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class OnbState(
    val language: AppLanguage = AppLanguage.RU,
    val baseCurrency: Currency = Currency.RUB,
    val rateUsd: String = "90",
    val rateEur: String = "100",
    val rateCny: String = "12",
    val rateKzt: String = "0.19",
    val pin: String = "",
    val biometric: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsRepo: SettingsRepository,
    private val setPinUseCase: SetPinUseCase,
    private val exchangeApi: ExchangeRateApi,
    private val appConfig: AppConfig,
) : ViewModel() {
    private val _state = MutableStateFlow(OnbState())
    val state: StateFlow<OnbState> = _state.asStateFlow()

    /** Whether the device has a registered biometric that can be offered during onboarding. */
    val biometricAvailable: Boolean = run {
        val result = BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        result != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
            result != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    private var ratesEdited = false

    init {
        // Prefill rates from the internet (built-in key) unless the user has already typed a rate.
        viewModelScope.launch {
            val key = resolveApiKey(null, appConfig) ?: return@launch
            when (val r = exchangeApi.fetchRates(key)) {
                is ExchangeResult.Success -> if (!ratesEdited) _state.update {
                    it.copy(rateUsd = fmt(r.usdRate), rateEur = fmt(r.eurRate), rateCny = fmt(r.cnyRate), rateKzt = fmt(r.kztRate))
                }
                is ExchangeResult.Failure -> Unit
            }
        }
    }

    private fun fmt(v: Double) = String.format(Locale.US, "%.2f", v)

    fun setLanguage(l: AppLanguage) = _state.update { it.copy(language = l) }
    fun setCurrency(c: Currency) = _state.update { it.copy(baseCurrency = c) }
    fun setRateUsd(v: String) { ratesEdited = true; _state.update { it.copy(rateUsd = v) } }
    fun setRateEur(v: String) { ratesEdited = true; _state.update { it.copy(rateEur = v) } }
    fun setRateCny(v: String) { ratesEdited = true; _state.update { it.copy(rateCny = v) } }
    fun setRateKzt(v: String) { ratesEdited = true; _state.update { it.copy(rateKzt = v) } }
    fun setPin(v: String) = _state.update { it.copy(pin = v.filter(Char::isDigit).take(4)) }
    fun toggleBiometric(b: Boolean) = _state.update { it.copy(biometric = b) }

    suspend fun finish() {
        val s = _state.value
        settingsRepo.setLanguage(s.language)
        settingsRepo.setBaseCurrency(s.baseCurrency)
        settingsRepo.setRates(
            s.rateUsd.toDoubleOrNull() ?: 90.0,
            s.rateEur.toDoubleOrNull() ?: 100.0,
            s.rateCny.toDoubleOrNull() ?: 12.0,
            s.rateKzt.toDoubleOrNull() ?: 0.19,
        )
        settingsRepo.setBiometricEnabled(s.biometric)
        if (s.pin.length == 4) setPinUseCase(s.pin)
        settingsRepo.setOnboardingCompleted(true)
    }

    /** True if the user has already entered anything beyond the defaults (rates, currency, pin). */
    fun hasUnsavedEdits(): Boolean = ratesEdited ||
        _state.value.pin.isNotEmpty() ||
        _state.value.biometric
}
