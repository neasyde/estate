package com.financeapp.core.domain.model

data class AppSettings(
    val baseCurrency: Currency = Currency.RUB,
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorScheme = ColorScheme.PURPLE,
    val pinHash: String? = null,
    val biometricEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.RU,
    val onboardingCompleted: Boolean = false,
    /** exchangerate-api.com v6 key; when null the app uses the manually entered rates. */
    val exchangeApiKey: String? = null,
    /** Epoch millis of the last successful rate update (0 = never). */
    val ratesUpdatedAt: Long = 0L,
    /** When true, rates are refreshed silently on app open once stale. */
    val autoRefreshRates: Boolean = true,
    /** How old (hours) cached rates may get before an auto-refresh is attempted. */
    val ratesIntervalHours: Int = 12,
    // --- display & behaviour ---
    val appFont: AppFont = AppFont.BRANDED,
    val fontSize: AppFontSize = AppFontSize.MEDIUM,
    val showDecimals: Boolean = true,
    val defaultTxType: TransactionType = TransactionType.EXPENSE,
    val animationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hideBalanceByDefault: Boolean = false,
    // --- security ---
    val requirePinOnLaunch: Boolean = true,
    val autoBiometric: Boolean = true,
    val autoLock: AutoLock = AutoLock.IMMEDIATE,
    // --- reminders defaults ---
    val defaultReminderHour: Int = 9,
    val defaultReminderLeadDays: Int = 1,
)
