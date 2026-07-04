package com.financeapp.core.backup

import android.content.Context
import android.net.Uri
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Exports/imports the whole database + portable settings to a JSON document via SAF uris. */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: FinanceDatabase,
    private val settingsRepo: SettingsRepository,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportTo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val s = settingsRepo.settings.first()
            val data = BackupData(
                exportedAt = System.currentTimeMillis(),
                transactions = db.transactionDao().observeAll().first(),
                categories = db.categoryDao().observeAll().first(),
                budgets = db.budgetDao().observeAll().first(),
                reminders = db.reminderDao().observeAll().first(),
                recurringRules = db.recurringRuleDao().observeAll().first(),
                settings = SettingsBackup(
                    baseCurrency = s.baseCurrency.name,
                    rateUsd = s.rateUsd,
                    rateEur = s.rateEur,
                    themeMode = s.themeMode.name,
                    colorScheme = s.colorScheme.name,
                    language = s.language.name,
                    autoRefreshRates = s.autoRefreshRates,
                    ratesIntervalHours = s.ratesIntervalHours,
                    showDecimals = s.showDecimals,
                    defaultTxType = s.defaultTxType.name,
                    animationsEnabled = s.animationsEnabled,
                    hapticsEnabled = s.hapticsEnabled,
                    hideBalanceByDefault = s.hideBalanceByDefault,
                    defaultReminderHour = s.defaultReminderHour,
                    defaultReminderLeadDays = s.defaultReminderLeadDays,
                ),
            )
            val text = json.encodeToString(BackupData.serializer(), data)
            val stream = context.contentResolver.openOutputStream(uri, "wt") ?: return@runCatching false
            stream.use { it.write(text.toByteArray()) }
            true
        }.getOrDefault(false)
    }

    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: return@runCatching false
            val data = json.decodeFromString(BackupData.serializer(), text)

            db.clearAllTables()
            // Categories first so transaction/budget category references resolve on read.
            data.categories.forEach { db.categoryDao().upsert(it) }
            data.transactions.forEach { db.transactionDao().upsert(it) }
            data.budgets.forEach { db.budgetDao().upsert(it) }
            data.reminders.forEach { db.reminderDao().upsert(it) }
            data.recurringRules.forEach { db.recurringRuleDao().upsert(it) }

            restoreSettings(data.settings)
            true
        }.getOrDefault(false)
    }

    /** Wipes all user data (all Room tables). Settings/PIN are left untouched. */
    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        runCatching { db.clearAllTables(); true }.getOrDefault(false)
    }

    private suspend fun restoreSettings(s: SettingsBackup) {
        runCatching { settingsRepo.setBaseCurrency(Currency.valueOf(s.baseCurrency)) }
        settingsRepo.setRates(s.rateUsd, s.rateEur)
        runCatching { settingsRepo.setThemeMode(ThemeMode.valueOf(s.themeMode)) }
        runCatching { settingsRepo.setColorScheme(ColorScheme.valueOf(s.colorScheme)) }
        runCatching { settingsRepo.setLanguage(AppLanguage.valueOf(s.language)) }
        settingsRepo.setAutoRefreshRates(s.autoRefreshRates)
        settingsRepo.setRatesIntervalHours(s.ratesIntervalHours)
        settingsRepo.setShowDecimals(s.showDecimals)
        runCatching { settingsRepo.setDefaultTxType(TransactionType.valueOf(s.defaultTxType)) }
        settingsRepo.setAnimationsEnabled(s.animationsEnabled)
        settingsRepo.setHapticsEnabled(s.hapticsEnabled)
        settingsRepo.setHideBalanceByDefault(s.hideBalanceByDefault)
        settingsRepo.setDefaultReminderHour(s.defaultReminderHour)
        settingsRepo.setDefaultReminderLeadDays(s.defaultReminderLeadDays)
    }
}
