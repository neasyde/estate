package com.financeapp.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.DeleteReminderUseCase
import com.financeapp.core.domain.usecase.ObserveRemindersUseCase
import com.financeapp.core.domain.usecase.SaveReminderUseCase
import com.financeapp.core.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    observeReminders: ObserveRemindersUseCase,
    private val saveReminder: SaveReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val scheduler: ReminderScheduler,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    // Re-arm alarms whenever the data changes or the screen is (re)opened — covers reboots too,
    // since AlarmManager alarms are cleared on restart and this fires on next app open.
    val reminders: StateFlow<List<Reminder>> = observeReminders()
        .onEach { scheduler.scheduleAll(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    /** (default notify hour, default days-before) for new reminders. */
    val reminderDefaults: StateFlow<Pair<Int, Int>> = settingsRepo.settings
        .map { it.defaultReminderHour to it.defaultReminderLeadDays }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9 to 1)

    fun save(reminder: Reminder) = viewModelScope.launch {
        val id = saveReminder(reminder)
        scheduler.schedule(reminder.copy(id = id))
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        scheduler.cancel(reminder.id)
        deleteReminder(reminder.id)
    }
}
