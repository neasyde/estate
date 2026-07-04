package com.financeapp.core.backup

import com.financeapp.core.data.local.entity.BudgetEntity
import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.data.local.entity.ReminderEntity
import com.financeapp.core.data.local.entity.TransactionEntity
import kotlinx.serialization.Serializable

/** Full snapshot of the app's data, written to / read from a user-picked JSON file (SAF). */
@Serializable
data class BackupData(
    val format: Int = 1,
    val exportedAt: Long = 0,
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val recurringRules: List<RecurringRuleEntity> = emptyList(),
    val settings: SettingsBackup = SettingsBackup(),
)

/**
 * Portable (non device-local) settings. PIN, biometric, api key, require-PIN and
 * auto-biometric flags are intentionally excluded. New fields are defaulted so older
 * backups still deserialize.
 */
@Serializable
data class SettingsBackup(
    val baseCurrency: String = "RUB",
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val themeMode: String = "SYSTEM",
    val colorScheme: String = "PURPLE",
    val language: String = "RU",
    val autoRefreshRates: Boolean = true,
    val ratesIntervalHours: Int = 12,
    val showDecimals: Boolean = true,
    val defaultTxType: String = "EXPENSE",
    val animationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hideBalanceByDefault: Boolean = false,
    val autoLock: String = "IMMEDIATE",
    val defaultReminderHour: Int = 9,
    val defaultReminderLeadDays: Int = 1,
)
