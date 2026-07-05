package com.financeapp.core.domain.repository

import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
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
    suspend fun setAppFont(f: AppFont)
    suspend fun setFontSize(s: AppFontSize)
    suspend fun setShowDecimals(b: Boolean)
    suspend fun setDefaultTxType(t: TransactionType)
    suspend fun setAnimationsEnabled(b: Boolean)
    suspend fun setHapticsEnabled(b: Boolean)
    suspend fun setHideBalanceByDefault(b: Boolean)
    suspend fun setRequirePinOnLaunch(b: Boolean)
    suspend fun setAutoBiometric(b: Boolean)
    suspend fun setAutoLock(a: AutoLock)
    suspend fun setDefaultReminderHour(h: Int)
    suspend fun setDefaultReminderLeadDays(d: Int)
}
