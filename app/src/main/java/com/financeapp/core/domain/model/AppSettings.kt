package com.financeapp.core.domain.model

data class AppSettings(
    val baseCurrency: Currency = Currency.RUB,
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val rateCny: Double = 12.0,
    val rateKzt: Double = 0.19,
    /** Rates for currencies not among the four above. Key = currency code, value = RUB per 1 unit. */
    val customRates: Map<String, Double> = emptyMap(),
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
    val hapticsEnabled: Boolean = false,
    val hideBalanceByDefault: Boolean = false,
    // --- security ---
    val requirePinOnLaunch: Boolean = true,
    val autoBiometric: Boolean = true,
    val autoLock: AutoLock = AutoLock.FIVE_MIN,
    // --- reminders defaults ---
    val defaultReminderHour: Int = 9,
    val defaultReminderLeadDays: Int = 1,
    // --- customization v2 ---
    val amoledMode: Boolean = false,
    val autoSwitchTheme: Boolean = false,
    val autoSwitchStart: Int = 22,
    val autoSwitchEnd: Int = 7,
    val customAccentColor: Int? = null,
    val dashboardLayout: DashboardLayout = DashboardLayout.DEFAULT,
    val dashboardBlocks: String = "all",
    /** Comma-separated list of visible quick action types on dashboard (e.g. "goals,projects,recurring"). Empty = all. */
    val dashboardQuickActions: String = "",
    // --- brute-force protection ---
    val failedAttempts: Int = 0,
    val lockoutUntil: Long = 0L,
)
