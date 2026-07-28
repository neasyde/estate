package com.financeapp.core.backup

import com.financeapp.core.data.local.entity.BudgetEntity
import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.data.local.entity.ReminderEntity
import com.financeapp.core.data.local.entity.TransactionEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportedAt: Long,
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val recurringRules: List<RecurringRuleEntity> = emptyList(),
    val dashboardQuickActions: String = "",
    // --- settings (v2) ---
    val baseCurrency: String = "RUB",
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val rateCny: Double = 12.0,
    val rateKzt: Double = 0.19,
    val themeMode: String = "SYSTEM",
    val colorScheme: String = "PURPLE",
    val language: String = "RU",
    val ratesUpdatedAt: Long = 0L,
    val autoRefreshRates: Boolean = true,
    val ratesIntervalHours: Int = 12,
    val appFont: String = "BRANDED",
    val fontSize: String = "MEDIUM",
    val showDecimals: Boolean = true,
    val defaultTxType: String = "EXPENSE",
    val animationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hideBalanceByDefault: Boolean = false,
    val defaultReminderHour: Int = 9,
    val defaultReminderLeadDays: Int = 1,
    val amoledMode: Boolean = false,
    val autoSwitchTheme: Boolean = false,
    val autoSwitchStart: Int = 22,
    val autoSwitchEnd: Int = 7,
    val customAccentColor: Int? = null,
    val dashboardLayout: String = "DEFAULT",
    val dashboardBlocks: String = "all",
    // --- security preferences (non-sensitive, transferred with backup) ---
    val biometricEnabled: Boolean = false,
    val requirePinOnLaunch: Boolean = true,
    val autoLock: String = "FIVE_MIN",
    val autoBiometric: Boolean = true,
)
