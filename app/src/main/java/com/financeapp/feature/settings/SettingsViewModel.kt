package com.financeapp.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.backup.BackupManager
import com.financeapp.core.config.AppConfig
import com.financeapp.core.data.remote.ExchangeRateApi
import com.financeapp.core.data.remote.ExchangeResult
import com.financeapp.core.domain.usecase.resolveApiKey
import com.financeapp.core.domain.usecase.shouldRefreshRates
import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.SetPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BackupEvent { EXPORT_OK, EXPORT_FAIL, IMPORT_OK, IMPORT_FAIL, CLEARED, CLEAR_FAIL }
enum class RatesEvent { UPDATED, NO_KEY, INVALID_KEY, FAILED }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val setPin: SetPinUseCase,
    private val backupManager: BackupManager,
    private val exchangeApi: ExchangeRateApi,
    private val appConfig: AppConfig,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _backupEvent = MutableStateFlow<BackupEvent?>(null)
    val backupEvent: StateFlow<BackupEvent?> = _backupEvent.asStateFlow()

    private val _ratesEvent = MutableStateFlow<RatesEvent?>(null)
    val ratesEvent: StateFlow<RatesEvent?> = _ratesEvent.asStateFlow()

    private val _ratesLoading = MutableStateFlow(false)
    val ratesLoading: StateFlow<Boolean> = _ratesLoading.asStateFlow()

    init {
        // Refresh silently on open when a key resolves, auto-refresh is on, and rates are stale.
        viewModelScope.launch {
            val s = settingsRepo.settings.first()
            val key = resolveApiKey(s.exchangeApiKey, appConfig)
            val intervalMs = s.ratesIntervalHours * 3_600_000L
            if (s.autoRefreshRates &&
                shouldRefreshRates(System.currentTimeMillis(), s.ratesUpdatedAt, intervalMs, key != null)
            ) {
                refreshRates(silent = true)
            }
        }
    }

    fun setTheme(m: ThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(m) }
    fun setScheme(s: ColorScheme) = viewModelScope.launch { settingsRepo.setColorScheme(s) }
    fun setLanguage(l: AppLanguage) = viewModelScope.launch { settingsRepo.setLanguage(l) }
    fun setBaseCurrency(c: Currency) = viewModelScope.launch { settingsRepo.setBaseCurrency(c) }
    fun setRates(usd: Double, eur: Double, cny: Double = 0.0, kzt: Double = 0.0) = viewModelScope.launch { settingsRepo.setRates(usd, eur, cny, kzt) }
    fun setBiometric(b: Boolean) = viewModelScope.launch { settingsRepo.setBiometricEnabled(b) }
    fun changePin(pin: String) = viewModelScope.launch { setPin(pin) }

    fun setAppFont(f: AppFont) = viewModelScope.launch { settingsRepo.setAppFont(f) }
    fun setFontSize(s: AppFontSize) = viewModelScope.launch { settingsRepo.setFontSize(s) }
    fun setShowDecimals(b: Boolean) = viewModelScope.launch { settingsRepo.setShowDecimals(b) }
    fun setDefaultTxType(t: TransactionType) = viewModelScope.launch { settingsRepo.setDefaultTxType(t) }
    fun setAnimations(b: Boolean) = viewModelScope.launch { settingsRepo.setAnimationsEnabled(b) }
    fun setHaptics(b: Boolean) = viewModelScope.launch { settingsRepo.setHapticsEnabled(b) }
    fun setHideBalance(b: Boolean) = viewModelScope.launch { settingsRepo.setHideBalanceByDefault(b) }
    fun setRequirePin(b: Boolean) = viewModelScope.launch { settingsRepo.setRequirePinOnLaunch(b) }
    fun setAutoBiometric(b: Boolean) = viewModelScope.launch { settingsRepo.setAutoBiometric(b) }
    fun setAutoLock(a: AutoLock) = viewModelScope.launch { settingsRepo.setAutoLock(a) }
    fun setReminderHour(h: Int) = viewModelScope.launch { settingsRepo.setDefaultReminderHour(h) }
    fun setReminderLeadDays(d: Int) = viewModelScope.launch { settingsRepo.setDefaultReminderLeadDays(d) }

    fun setAmoledMode(b: Boolean) = viewModelScope.launch { settingsRepo.setAmoledMode(b) }
    fun setAutoSwitchTheme(b: Boolean) = viewModelScope.launch { settingsRepo.setAutoSwitchTheme(b) }
    fun setAutoSwitchStart(h: Int) = viewModelScope.launch { settingsRepo.setAutoSwitchStart(h) }
    fun setAutoSwitchEnd(h: Int) = viewModelScope.launch { settingsRepo.setAutoSwitchEnd(h) }

    fun clearAllData() = viewModelScope.launch {
        _backupEvent.value = if (backupManager.clearAllData()) BackupEvent.CLEARED else BackupEvent.CLEAR_FAIL
    }

    fun setApiKey(key: String) = viewModelScope.launch { settingsRepo.setExchangeApiKey(key) }

    fun setAutoRefresh(enabled: Boolean) = viewModelScope.launch { settingsRepo.setAutoRefreshRates(enabled) }
    fun setRatesInterval(hours: Int) = viewModelScope.launch { settingsRepo.setRatesIntervalHours(hours) }

    fun refreshRates(silent: Boolean = false) = viewModelScope.launch {
        val key = resolveApiKey(settingsRepo.settings.first().exchangeApiKey, appConfig)
        if (key.isNullOrBlank()) {
            if (!silent) _ratesEvent.value = RatesEvent.NO_KEY
            return@launch
        }
        _ratesLoading.value = true
        when (val r = exchangeApi.fetchRates(key)) {
            is ExchangeResult.Success -> {
                settingsRepo.setRates(r.usdRate, r.eurRate, r.cnyRate, r.kztRate)
                if (!silent) _ratesEvent.value = RatesEvent.UPDATED
            }
            is ExchangeResult.Failure -> if (!silent) {
                _ratesEvent.value = when (r.type) {
                    "invalid-key", "inactive-account" -> RatesEvent.INVALID_KEY
                    else -> RatesEvent.FAILED
                }
            }
        }
        _ratesLoading.value = false
    }

    fun clearRatesEvent() { _ratesEvent.value = null }

    fun exportBackup(uri: Uri) = viewModelScope.launch {
        _backupEvent.value = if (backupManager.exportTo(uri)) BackupEvent.EXPORT_OK else BackupEvent.EXPORT_FAIL
    }

    fun exportBackupEncrypted(uri: Uri) = viewModelScope.launch {
        _backupEvent.value = if (backupManager.exportToEncrypted(uri)) BackupEvent.EXPORT_OK else BackupEvent.EXPORT_FAIL
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        _backupEvent.value = if (backupManager.importFrom(uri)) BackupEvent.IMPORT_OK else BackupEvent.IMPORT_FAIL
    }

    fun clearBackupEvent() { _backupEvent.value = null }
}
