package com.financeapp.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DashboardLayout
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.SecureKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: FinanceDatabase,
    private val settingsRepo: SettingsRepository,
    @Named("pretty") private val prettyJson: Json,
    @Named("default") private val defaultJson: Json,
) {
    suspend fun exportTo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val s = settingsRepo.settings.first()
            val data = BackupData(
                exportedAt = System.currentTimeMillis(),
                transactions = db.transactionDao().allOnce(),
                categories = db.categoryDao().allOnce(),
                budgets = db.budgetDao().allOnce(),
                reminders = db.reminderDao().allOnce(),
                recurringRules = db.recurringRuleDao().allOnce(),
                dashboardQuickActions = s.dashboardQuickActions,
                baseCurrency = s.baseCurrency.name,
                rateUsd = s.rateUsd,
                rateEur = s.rateEur,
                rateCny = s.rateCny,
                rateKzt = s.rateKzt,
                themeMode = s.themeMode.name,
                colorScheme = s.colorScheme.name,
                language = s.language.name,
                // exchangeApiKey intentionally excluded — sensitive
                ratesUpdatedAt = s.ratesUpdatedAt,
                autoRefreshRates = s.autoRefreshRates,
                ratesIntervalHours = s.ratesIntervalHours,
                appFont = s.appFont.name,
                fontSize = s.fontSize.name,
                showDecimals = s.showDecimals,
                defaultTxType = s.defaultTxType.name,
                animationsEnabled = s.animationsEnabled,
                hapticsEnabled = s.hapticsEnabled,
                hideBalanceByDefault = s.hideBalanceByDefault,
                // Security settings intentionally excluded — should not transfer between devices
                defaultReminderHour = s.defaultReminderHour,
                defaultReminderLeadDays = s.defaultReminderLeadDays,
                amoledMode = s.amoledMode,
                autoSwitchTheme = s.autoSwitchTheme,
                autoSwitchStart = s.autoSwitchStart,
                autoSwitchEnd = s.autoSwitchEnd,
                customAccentColor = s.customAccentColor,
                dashboardLayout = s.dashboardLayout.name,
                dashboardBlocks = s.dashboardBlocks,
                // Security preferences — non-sensitive, transferred with backup
                biometricEnabled = s.biometricEnabled,
                requirePinOnLaunch = s.requirePinOnLaunch,
                autoLock = s.autoLock.name,
                autoBiometric = s.autoBiometric,
            )
            val json = prettyJson.encodeToString(BackupData.serializer(), data)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
                os.flush()
            } ?: return@runCatching false
            true
        }.getOrDefault(false)
    }

    suspend fun exportToEncrypted(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val s = settingsRepo.settings.first()
            val data = BackupData(
                exportedAt = System.currentTimeMillis(),
                transactions = db.transactionDao().allOnce(),
                categories = db.categoryDao().allOnce(),
                budgets = db.budgetDao().allOnce(),
                reminders = db.reminderDao().allOnce(),
                recurringRules = db.recurringRuleDao().allOnce(),
                dashboardQuickActions = s.dashboardQuickActions,
                baseCurrency = s.baseCurrency.name,
                rateUsd = s.rateUsd,
                rateEur = s.rateEur,
                rateCny = s.rateCny,
                rateKzt = s.rateKzt,
                themeMode = s.themeMode.name,
                colorScheme = s.colorScheme.name,
                language = s.language.name,
                ratesUpdatedAt = s.ratesUpdatedAt,
                autoRefreshRates = s.autoRefreshRates,
                ratesIntervalHours = s.ratesIntervalHours,
                appFont = s.appFont.name,
                fontSize = s.fontSize.name,
                showDecimals = s.showDecimals,
                defaultTxType = s.defaultTxType.name,
                animationsEnabled = s.animationsEnabled,
                hapticsEnabled = s.hapticsEnabled,
                hideBalanceByDefault = s.hideBalanceByDefault,
                defaultReminderHour = s.defaultReminderHour,
                defaultReminderLeadDays = s.defaultReminderLeadDays,
                amoledMode = s.amoledMode,
                autoSwitchTheme = s.autoSwitchTheme,
                autoSwitchStart = s.autoSwitchStart,
                autoSwitchEnd = s.autoSwitchEnd,
                customAccentColor = s.customAccentColor,
                dashboardLayout = s.dashboardLayout.name,
                dashboardBlocks = s.dashboardBlocks,
                biometricEnabled = s.biometricEnabled,
                requirePinOnLaunch = s.requirePinOnLaunch,
                autoLock = s.autoLock.name,
                autoBiometric = s.autoBiometric,
            )
            val json = prettyJson.encodeToString(BackupData.serializer(), data)
            val encrypted = SecureKeyStore.encrypt(json)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(encrypted.toByteArray(Charsets.UTF_8))
                os.flush()
            } ?: return@runCatching false
            true
        }.getOrDefault(false)
    }

    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: return@runCatching false

            val jsonText = try {
                SecureKeyStore.decrypt(text)
            } catch (_: Exception) {
                null
            }

            val finalText = jsonText ?: text
            val data = defaultJson.decodeFromString(BackupData.serializer(), finalText)
            if (data.version < 1 || data.version > CURRENT_BACKUP_VERSION) return@runCatching false

            db.withTransaction {
                db.clearAllTables()
                data.categories.forEach { db.categoryDao().upsert(it) }
                data.recurringRules.forEach { db.recurringRuleDao().upsert(it) }
                data.transactions.forEach { db.transactionDao().upsert(it) }
                data.budgets.forEach { db.budgetDao().upsert(it) }
                data.reminders.forEach { db.reminderDao().upsert(it) }
            }

            // Snapshot security settings before clearAll — same device may re-encrypt pinHash
            val s = settingsRepo.settings.first()
            val secPinHash = s.pinHash
            val secBiometric = s.biometricEnabled
            val secRequirePin = s.requirePinOnLaunch
            val secAutoLock = s.autoLock
            val secAutoBiometric = s.autoBiometric

            settingsRepo.clearAll()
            runCatching {
                settingsRepo.setOnboardingCompleted(true)
                // Restore security on same device (pinHash decryptable by same SecureKeyStore key)
                settingsRepo.setPinHash(secPinHash)
                settingsRepo.setBiometricEnabled(secBiometric)
                settingsRepo.setRequirePinOnLaunch(secRequirePin)
                settingsRepo.setAutoLock(secAutoLock)
                settingsRepo.setAutoBiometric(secAutoBiometric)

                if (data.dashboardQuickActions.isNotEmpty()) {
                    settingsRepo.setDashboardQuickActions(data.dashboardQuickActions)
                }
                runCatching {
                    settingsRepo.setBaseCurrency(Currency.valueOf(data.baseCurrency))
                }
                // Do NOT restore rates from backup — they may be stale.
                // exchangeApiKey is not restored (sensitive), so auto-refresh needs a fresh fetch.
                // Set ratesUpdatedAt = 0 to trigger rate refresh on next launch.
                settingsRepo.setRatesUpdatedAt(0L)
                runCatching { settingsRepo.setThemeMode(ThemeMode.valueOf(data.themeMode)) }
                runCatching { settingsRepo.setColorScheme(ColorScheme.valueOf(data.colorScheme)) }
                runCatching { settingsRepo.setLanguage(AppLanguage.valueOf(data.language)) }
                settingsRepo.setAutoRefreshRates(data.autoRefreshRates)
                settingsRepo.setRatesIntervalHours(data.ratesIntervalHours)
                runCatching { settingsRepo.setAppFont(AppFont.valueOf(data.appFont)) }
                runCatching { settingsRepo.setFontSize(AppFontSize.valueOf(data.fontSize)) }
                settingsRepo.setShowDecimals(data.showDecimals)
                runCatching { settingsRepo.setDefaultTxType(TransactionType.valueOf(data.defaultTxType)) }
                settingsRepo.setAnimationsEnabled(data.animationsEnabled)
                settingsRepo.setHapticsEnabled(data.hapticsEnabled)
                settingsRepo.setHideBalanceByDefault(data.hideBalanceByDefault)
                settingsRepo.setDefaultReminderHour(data.defaultReminderHour)
                settingsRepo.setDefaultReminderLeadDays(data.defaultReminderLeadDays)
                settingsRepo.setAmoledMode(data.amoledMode)
                settingsRepo.setAutoSwitchTheme(data.autoSwitchTheme)
                settingsRepo.setAutoSwitchStart(data.autoSwitchStart)
                settingsRepo.setAutoSwitchEnd(data.autoSwitchEnd)
                settingsRepo.setCustomAccentColor(data.customAccentColor)
                runCatching { settingsRepo.setDashboardLayout(DashboardLayout.valueOf(data.dashboardLayout)) }
                settingsRepo.setDashboardBlocks(data.dashboardBlocks)
            }

            true
        }.getOrDefault(false)
    }

    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            // Read settings once to avoid TOCTOU race
            val s = settingsRepo.settings.first()
            val secSnapshot = Triple(s.pinHash, s.biometricEnabled, s.requirePinOnLaunch)
            val autoLockVal = s.autoLock
            val autoBiometricVal = s.autoBiometric
            val onbCompleted = s.onboardingCompleted

            db.withTransaction {
                db.transactionDao().deleteAll()
                db.budgetDao().deleteAll()
                db.reminderDao().deleteAll()
                db.recurringRuleDao().deleteAll()
                // Categories are intentionally kept — they are seed/defaults + user-created.
                // clearAllData resets transactions/data, not a factory reset.
            }
            settingsRepo.clearAll()
            settingsRepo.setPinHash(secSnapshot.first)
            settingsRepo.setBiometricEnabled(secSnapshot.second)
            settingsRepo.setRequirePinOnLaunch(secSnapshot.third)
            settingsRepo.setAutoLock(autoLockVal)
            settingsRepo.setAutoBiometric(autoBiometricVal)
            settingsRepo.setOnboardingCompleted(onbCompleted)
            true
        }.getOrDefault(false)
    }

    private companion object {
        const val CURRENT_BACKUP_VERSION = 2
    }
}
