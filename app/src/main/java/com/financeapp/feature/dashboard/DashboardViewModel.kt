package com.financeapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.config.AppConfig
import com.financeapp.core.data.remote.ExchangeRateApi
import com.financeapp.core.data.remote.ExchangeResult
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DashboardData
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.DeleteReminderUseCase
import com.financeapp.core.domain.usecase.GetDashboardDataUseCase
import com.financeapp.core.domain.usecase.ObserveRemindersUseCase
import com.financeapp.core.domain.usecase.SaveReminderUseCase
import com.financeapp.core.domain.usecase.resolveApiKey
import com.financeapp.core.domain.usecase.shouldRefreshRates
import com.financeapp.core.reminders.ReminderIntent
import com.financeapp.core.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/** Currency rates shown on Home. [updatedAt] == 0 means "never fetched". */
data class RatesUi(val usd: Double = 0.0, val eur: Double = 0.0, val updatedAt: Long = 0L)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboard: GetDashboardDataUseCase,
    observeReminders: ObserveRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val scheduler: ReminderScheduler,
    private val settingsRepo: SettingsRepository,
    private val exchangeApi: ExchangeRateApi,
    private val appConfig: AppConfig,
) : ViewModel() {
    val state: StateFlow<DashboardData> = getDashboard(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())

    val rates: StateFlow<RatesUi> = settingsRepo.settings
        .map { RatesUi(it.rateUsd, it.rateEur, it.ratesUpdatedAt) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RatesUi())

    private val refreshedOnce = AtomicBoolean(false)

    init {
        // Silent one-shot auto-refresh when Home opens: fetch fresh rates if a key resolves,
        // auto-refresh is on, and the cached rates are stale. Failures stay silent on Home.
        viewModelScope.launch {
            val s = settingsRepo.settings.first()
            _balanceHidden.value = s.hideBalanceByDefault
            val key = resolveApiKey(s.exchangeApiKey, appConfig) ?: return@launch
            val intervalMs = s.ratesIntervalHours * 3_600_000L
            if (s.autoRefreshRates &&
                shouldRefreshRates(System.currentTimeMillis(), s.ratesUpdatedAt, intervalMs, hasKey = true) &&
                refreshedOnce.compareAndSet(false, true)
            ) {
                when (val r = exchangeApi.fetchRates(key)) {
                    is ExchangeResult.Success -> settingsRepo.setRates(r.usdRate, r.eurRate)
                    is ExchangeResult.Failure -> Unit
                }
            }
        }
    }

    // Show the three soonest reminders on home; re-arm alarms on open (covers reboots, since
    // home is the first screen the user lands on).
    val upcomingReminders: StateFlow<List<Reminder>> = observeReminders()
        .onEach { scheduler.scheduleAll(it) }
        .map { list -> list.sortedBy { ReminderIntent.triggerAt(it) ?: Long.MAX_VALUE }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    val defaultTxType: StateFlow<TransactionType> = settingsRepo.settings
        .map { it.defaultTxType }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionType.EXPENSE)

    /** (default notify hour, default days-before) for new reminders. */
    val reminderDefaults: StateFlow<Pair<Int, Int>> = settingsRepo.settings
        .map { it.defaultReminderHour to it.defaultReminderLeadDays }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9 to 1)

    private val _balanceHidden = MutableStateFlow(false)
    val balanceHidden: StateFlow<Boolean> = _balanceHidden.asStateFlow()

    fun toggleBalanceHidden() = _balanceHidden.update { !it }

    fun saveReminder(reminder: Reminder) = viewModelScope.launch {
        val id = saveReminderUseCase(reminder)
        scheduler.schedule(reminder.copy(id = id))
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        scheduler.cancel(reminder.id)
        deleteReminderUseCase(reminder.id)
    }
}
