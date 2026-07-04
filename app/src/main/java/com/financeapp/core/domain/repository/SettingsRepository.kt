package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setBaseCurrency(c: Currency)
    suspend fun setRates(usd: Double, eur: Double)
    suspend fun setAutoRefreshRates(enabled: Boolean)
    suspend fun setRatesIntervalHours(hours: Int)
    suspend fun setExchangeApiKey(key: String?)
    suspend fun setThemeMode(m: ThemeMode)
    suspend fun setColorScheme(s: ColorScheme)
    suspend fun setLanguage(l: AppLanguage)
    suspend fun setPinHash(hash: String?)
    suspend fun setBiometricEnabled(b: Boolean)
    suspend fun setOnboardingCompleted(b: Boolean)
}
