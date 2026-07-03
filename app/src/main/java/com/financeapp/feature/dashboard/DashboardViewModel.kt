package com.financeapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.DashboardData
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.DeleteReminderUseCase
import com.financeapp.core.domain.usecase.GetDashboardDataUseCase
import com.financeapp.core.domain.usecase.ObserveRemindersUseCase
import com.financeapp.core.domain.usecase.SaveReminderUseCase
import com.financeapp.core.reminders.ReminderIntent
import com.financeapp.core.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboard: GetDashboardDataUseCase,
    observeReminders: ObserveRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val scheduler: ReminderScheduler,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<DashboardData> = getDashboard(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())

    // Show the three soonest reminders on home; re-arm alarms on open (covers reboots, since
    // home is the first screen the user lands on).
    val upcomingReminders: StateFlow<List<Reminder>> = observeReminders()
        .onEach { scheduler.scheduleAll(it) }
        .map { list -> list.sortedBy { ReminderIntent.triggerAt(it) ?: Long.MAX_VALUE }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

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
