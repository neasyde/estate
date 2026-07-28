package com.financeapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.financeapp.core.data.datastore.SettingsKeys
import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DashboardLayout
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.SecureKeyStore
import com.financeapp.core.utils.safeValueOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        val def = AppSettings()
        AppSettings(
            baseCurrency = p[SettingsKeys.BASE_CURRENCY]?.let { safeValueOf(it, def.baseCurrency) } ?: def.baseCurrency,
            rateUsd = p[SettingsKeys.RATE_USD] ?: def.rateUsd,
            rateEur = p[SettingsKeys.RATE_EUR] ?: def.rateEur,
            rateCny = p[SettingsKeys.RATE_CNY] ?: def.rateCny,
            rateKzt = p[SettingsKeys.RATE_KZT] ?: def.rateKzt,
            themeMode = p[SettingsKeys.THEME_MODE]?.let { safeValueOf(it, def.themeMode) } ?: def.themeMode,
            colorScheme = p[SettingsKeys.COLOR_SCHEME]?.let { safeValueOf(it, def.colorScheme) } ?: def.colorScheme,
            pinHash = p[SettingsKeys.PIN_HASH]?.let { SecureKeyStore.decrypt(it) },
            biometricEnabled = p[SettingsKeys.BIOMETRIC] ?: def.biometricEnabled,
            language = p[SettingsKeys.LANGUAGE]?.let { safeValueOf(it, def.language) } ?: def.language,
            onboardingCompleted = p[SettingsKeys.ONBOARDING] ?: def.onboardingCompleted,
            exchangeApiKey = p[SettingsKeys.EXCHANGE_API_KEY]?.let { SecureKeyStore.decrypt(it) }?.takeIf { it.isNotBlank() },
            ratesUpdatedAt = p[SettingsKeys.RATES_UPDATED_AT] ?: def.ratesUpdatedAt,
            autoRefreshRates = p[SettingsKeys.AUTO_REFRESH] ?: def.autoRefreshRates,
            ratesIntervalHours = p[SettingsKeys.RATES_INTERVAL] ?: def.ratesIntervalHours,
            appFont = p[SettingsKeys.APP_FONT]?.let { runCatching { AppFont.valueOf(it) }.getOrNull() } ?: def.appFont,
            fontSize = p[SettingsKeys.FONT_SIZE]?.let { runCatching { AppFontSize.valueOf(it) }.getOrNull() } ?: def.fontSize,
            showDecimals = p[SettingsKeys.SHOW_DECIMALS] ?: def.showDecimals,
            defaultTxType = p[SettingsKeys.DEFAULT_TX_TYPE]?.let { safeValueOf(it, def.defaultTxType) } ?: def.defaultTxType,
            animationsEnabled = p[SettingsKeys.ANIMATIONS] ?: def.animationsEnabled,
            hapticsEnabled = p[SettingsKeys.HAPTICS] ?: def.hapticsEnabled,
            hideBalanceByDefault = p[SettingsKeys.HIDE_BALANCE] ?: def.hideBalanceByDefault,
            requirePinOnLaunch = p[SettingsKeys.REQUIRE_PIN] ?: def.requirePinOnLaunch,
            autoBiometric = p[SettingsKeys.AUTO_BIOMETRIC] ?: def.autoBiometric,
            autoLock = p[SettingsKeys.AUTO_LOCK]?.let { safeValueOf(it, def.autoLock) } ?: def.autoLock,
            defaultReminderHour = p[SettingsKeys.REMINDER_HOUR] ?: def.defaultReminderHour,
            defaultReminderLeadDays = p[SettingsKeys.REMINDER_LEAD_DAYS] ?: def.defaultReminderLeadDays,
            amoledMode = p[SettingsKeys.AMOLED_MODE] ?: def.amoledMode,
            autoSwitchTheme = p[SettingsKeys.AUTO_SWITCH_THEME] ?: def.autoSwitchTheme,
            autoSwitchStart = p[SettingsKeys.AUTO_SWITCH_START] ?: def.autoSwitchStart,
            autoSwitchEnd = p[SettingsKeys.AUTO_SWITCH_END] ?: def.autoSwitchEnd,
            customAccentColor = p[SettingsKeys.CUSTOM_ACCENT_COLOR],
            dashboardLayout = p[SettingsKeys.DASHBOARD_LAYOUT]?.let { runCatching { DashboardLayout.valueOf(it) }.getOrNull() } ?: def.dashboardLayout,
            dashboardBlocks = p[SettingsKeys.DASHBOARD_BLOCKS] ?: def.dashboardBlocks,
            dashboardQuickActions = p[SettingsKeys.DASHBOARD_QUICK_ACTIONS] ?: def.dashboardQuickActions,
            // SECURITY NOTE: failedAttempts and lockoutUntil are stored in plaintext DataStore.
            // An attacker with root access could reset these. Full encryption would require
            // SecureKeyStore to support non-string types. For now, this is acceptable because
            // brute-force protection is a UX deterrent, not a cryptographic guarantee.
            failedAttempts = p[SettingsKeys.FAILED_ATTEMPTS] ?: def.failedAttempts,
            lockoutUntil = p[SettingsKeys.LOCKOUT_UNTIL] ?: def.lockoutUntil,
        )
    }

    override suspend fun setBaseCurrency(c: Currency) {
        dataStore.edit { it[SettingsKeys.BASE_CURRENCY] = c.name }
    }

    override suspend fun setRates(usd: Double, eur: Double, cny: Double, kzt: Double) {
        dataStore.edit {
            if (usd in 0.0001..1_000_000.0 && usd.isFinite()) it[SettingsKeys.RATE_USD] = usd
            if (eur in 0.0001..1_000_000.0 && eur.isFinite()) it[SettingsKeys.RATE_EUR] = eur
            if (cny in 0.0001..1_000_000.0 && cny.isFinite()) it[SettingsKeys.RATE_CNY] = cny
            if (kzt in 0.0001..1_000_000.0 && kzt.isFinite()) it[SettingsKeys.RATE_KZT] = kzt
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
            if (trimmed.isEmpty()) it.remove(SettingsKeys.EXCHANGE_API_KEY) else it[SettingsKeys.EXCHANGE_API_KEY] = SecureKeyStore.encrypt(trimmed)
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
            if (hash == null) it.remove(SettingsKeys.PIN_HASH) else it[SettingsKeys.PIN_HASH] = SecureKeyStore.encrypt(hash)
        }
    }

    override suspend fun setBiometricEnabled(b: Boolean) {
        dataStore.edit { it[SettingsKeys.BIOMETRIC] = b }
    }

    override suspend fun setOnboardingCompleted(b: Boolean) {
        dataStore.edit { it[SettingsKeys.ONBOARDING] = b }
    }

    override suspend fun setAppFont(f: AppFont) {
        dataStore.edit { it[SettingsKeys.APP_FONT] = f.name }
    }

    override suspend fun setFontSize(s: AppFontSize) {
        dataStore.edit { it[SettingsKeys.FONT_SIZE] = s.name }
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

    override suspend fun setAmoledMode(b: Boolean) {
        dataStore.edit { it[SettingsKeys.AMOLED_MODE] = b }
    }

    override suspend fun setAutoSwitchTheme(b: Boolean) {
        dataStore.edit { it[SettingsKeys.AUTO_SWITCH_THEME] = b }
    }

    override suspend fun setAutoSwitchStart(h: Int) {
        dataStore.edit { it[SettingsKeys.AUTO_SWITCH_START] = h }
    }

    override suspend fun setAutoSwitchEnd(h: Int) {
        dataStore.edit { it[SettingsKeys.AUTO_SWITCH_END] = h }
    }

    override suspend fun setCustomAccentColor(color: Int?) {
        dataStore.edit {
            if (color == null) it.remove(SettingsKeys.CUSTOM_ACCENT_COLOR) else it[SettingsKeys.CUSTOM_ACCENT_COLOR] = color
        }
    }

    override suspend fun setDashboardLayout(l: DashboardLayout) {
        dataStore.edit { it[SettingsKeys.DASHBOARD_LAYOUT] = l.name }
    }

    override suspend fun setDashboardBlocks(blocks: String) {
        dataStore.edit { it[SettingsKeys.DASHBOARD_BLOCKS] = blocks }
    }

    override suspend fun setDashboardQuickActions(actions: String) {
        dataStore.edit { it[SettingsKeys.DASHBOARD_QUICK_ACTIONS] = actions }
    }

    override suspend fun setRatesUpdatedAt(timestamp: Long) {
        dataStore.edit { it[SettingsKeys.RATES_UPDATED_AT] = timestamp }
    }

    override suspend fun setFailedAttempts(attempts: Int) {
        dataStore.edit { it[SettingsKeys.FAILED_ATTEMPTS] = attempts }
    }

    override suspend fun setLockoutUntil(timestamp: Long) {
        dataStore.edit { it[SettingsKeys.LOCKOUT_UNTIL] = timestamp }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
