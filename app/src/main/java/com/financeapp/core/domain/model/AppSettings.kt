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
)
