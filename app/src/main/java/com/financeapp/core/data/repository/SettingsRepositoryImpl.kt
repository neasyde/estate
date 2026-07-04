package com.financeapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.financeapp.core.data.datastore.SettingsKeys
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        val def = AppSettings()
        AppSettings(
            baseCurrency = p[SettingsKeys.BASE_CURRENCY]?.let { Currency.valueOf(it) } ?: def.baseCurrency,
            rateUsd = p[SettingsKeys.RATE_USD] ?: def.rateUsd,
            rateEur = p[SettingsKeys.RATE_EUR] ?: def.rateEur,
            themeMode = p[SettingsKeys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: def.themeMode,
            colorScheme = p[SettingsKeys.COLOR_SCHEME]?.let { ColorScheme.valueOf(it) } ?: def.colorScheme,
            pinHash = p[SettingsKeys.PIN_HASH],
            biometricEnabled = p[SettingsKeys.BIOMETRIC] ?: def.biometricEnabled,
            language = p[SettingsKeys.LANGUAGE]?.let { AppLanguage.valueOf(it) } ?: def.language,
            onboardingCompleted = p[SettingsKeys.ONBOARDING] ?: def.onboardingCompleted,
            exchangeApiKey = p[SettingsKeys.EXCHANGE_API_KEY]?.takeIf { it.isNotBlank() },
            ratesUpdatedAt = p[SettingsKeys.RATES_UPDATED_AT] ?: def.ratesUpdatedAt,
            autoRefreshRates = p[SettingsKeys.AUTO_REFRESH] ?: def.autoRefreshRates,
            ratesIntervalHours = p[SettingsKeys.RATES_INTERVAL] ?: def.ratesIntervalHours,
            showDecimals = p[SettingsKeys.SHOW_DECIMALS] ?: def.showDecimals,
            defaultTxType = p[SettingsKeys.DEFAULT_TX_TYPE]?.let { TransactionType.valueOf(it) } ?: def.defaultTxType,
            animationsEnabled = p[SettingsKeys.ANIMATIONS] ?: def.animationsEnabled,
            hapticsEnabled = p[SettingsKeys.HAPTICS] ?: def.hapticsEnabled,
            hideBalanceByDefault = p[SettingsKeys.HIDE_BALANCE] ?: def.hideBalanceByDefault,
            requirePinOnLaunch = p[SettingsKeys.REQUIRE_PIN] ?: def.requirePinOnLaunch,
            autoBiometric = p[SettingsKeys.AUTO_BIOMETRIC] ?: def.autoBiometric,
            autoLock = p[SettingsKeys.AUTO_LOCK]?.let { AutoLock.valueOf(it) } ?: def.autoLock,
            defaultReminderHour = p[SettingsKeys.REMINDER_HOUR] ?: def.defaultReminderHour,
            defaultReminderLeadDays = p[SettingsKeys.REMINDER_LEAD_DAYS] ?: def.defaultReminderLeadDays,
        )
    }

    override suspend fun setBaseCurrency(c: Currency) {
        dataStore.edit { it[SettingsKeys.BASE_CURRENCY] = c.name }
    }

    override suspend fun setRates(usd: Double, eur: Double) {
        dataStore.edit {
            it[SettingsKeys.RATE_USD] = usd
            it[SettingsKeys.RATE_EUR] = eur
            it[SettingsKeys.RATES_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    override suspend fun setAutoRefreshRates(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.AUTO_REFRESH] = enabled }
    }

    override suspend fun setRatesIntervalHours(hours: Int) {
        dataStore.edit { it[SettingsKeys.RATES_INTERVAL] = hours }
    }

    override suspend fun setExchangeApiKey(key: String?) {
        dataStore.edit {
            val trimmed = key?.trim().orEmpty()
            if (trimmed.isEmpty()) it.remove(SettingsKeys.EXCHANGE_API_KEY) else it[SettingsKeys.EXCHANGE_API_KEY] = trimmed
        }
    }

    override suspend fun setThemeMode(m: ThemeMode) {
        dataStore.edit { it[SettingsKeys.THEME_MODE] = m.name }
    }

    override suspend fun setColorScheme(s: ColorScheme) {
        dataStore.edit { it[SettingsKeys.COLOR_SCHEME] = s.name }
    }

    override suspend fun setLanguage(l: AppLanguage) {
        dataStore.edit { it[SettingsKeys.LANGUAGE] = l.name }
    }

    override suspend fun setPinHash(hash: String?) {
        dataStore.edit {
            if (hash == null) it.remove(SettingsKeys.PIN_HASH) else it[SettingsKeys.PIN_HASH] = hash
        }
    }

    override suspend fun setBiometricEnabled(b: Boolean) {
        dataStore.edit { it[SettingsKeys.BIOMETRIC] = b }
    }

    override suspend fun setOnboardingCompleted(b: Boolean) {
        dataStore.edit { it[SettingsKeys.ONBOARDING] = b }
    }

    override suspend fun setShowDecimals(b: Boolean) {
        dataStore.edit { it[SettingsKeys.SHOW_DECIMALS] = b }
    }

    override suspend fun setDefaultTxType(t: TransactionType) {
        dataStore.edit { it[SettingsKeys.DEFAULT_TX_TYPE] = t.name }
    }

    override suspend fun setAnimationsEnabled(b: Boolean) {
        dataStore.edit { it[SettingsKeys.ANIMATIONS] = b }
    }

    override suspend fun setHapticsEnabled(b: Boolean) {
        dataStore.edit { it[SettingsKeys.HAPTICS] = b }
    }

    override suspend fun setHideBalanceByDefault(b: Boolean) {
        dataStore.edit { it[SettingsKeys.HIDE_BALANCE] = b }
    }

    override suspend fun setRequirePinOnLaunch(b: Boolean) {
        dataStore.edit { it[SettingsKeys.REQUIRE_PIN] = b }
    }

    override suspend fun setAutoBiometric(b: Boolean) {
        dataStore.edit { it[SettingsKeys.AUTO_BIOMETRIC] = b }
    }

    override suspend fun setAutoLock(a: AutoLock) {
        dataStore.edit { it[SettingsKeys.AUTO_LOCK] = a.name }
    }

    override suspend fun setDefaultReminderHour(h: Int) {
        dataStore.edit { it[SettingsKeys.REMINDER_HOUR] = h }
    }

    override suspend fun setDefaultReminderLeadDays(d: Int) {
        dataStore.edit { it[SettingsKeys.REMINDER_LEAD_DAYS] = d }
    }
}
