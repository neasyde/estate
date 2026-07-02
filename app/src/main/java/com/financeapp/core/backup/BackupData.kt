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

/** Portable (non device-local) settings. PIN, biometric and onboarding flags are intentionally excluded. */
@Serializable
data class SettingsBackup(
    val baseCurrency: String = "RUB",
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val themeMode: String = "SYSTEM",
    val colorScheme: String = "PURPLE",
    val language: String = "RU",
)
