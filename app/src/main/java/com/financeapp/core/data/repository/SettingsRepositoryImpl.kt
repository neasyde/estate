package com.financeapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.financeapp.core.data.datastore.SettingsKeys
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
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
        )
    }

    override suspend fun setBaseCurrency(c: Currency) {
        dataStore.edit { it[SettingsKeys.BASE_CURRENCY] = c.name }
    }

    override suspend fun setRates(usd: Double, eur: Double) {
        dataStore.edit { it[SettingsKeys.RATE_USD] = usd; it[SettingsKeys.RATE_EUR] = eur }
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
}
