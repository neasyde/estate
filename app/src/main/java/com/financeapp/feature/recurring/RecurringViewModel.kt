package com.financeapp.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.data.local.dao.RecurringRuleDao
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val dao: RecurringRuleDao,
    @Named("default") private val json: Json,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    /** Exposed so the screen can pass the shared [Json] into the AddEditRecurringSheet. */
    val sharedJson: Json get() = json

    val rules: StateFlow<List<RecurringRuleEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    fun addRule(templateJson: String, interval: IntervalType, nextDate: Long, autoAdd: Boolean) =
        viewModelScope.launch {
            dao.upsert(
                RecurringRuleEntity(
                    templateJson = templateJson,
                    intervalType = interval.name,
                    nextDate = nextDate,
                    autoAdd = autoAdd,
                ),
            )
        }

    fun updateRule(rule: RecurringRuleEntity, templateJson: String, interval: IntervalType, nextDate: Long, autoAdd: Boolean) =
        viewModelScope.launch {
            dao.upsert(rule.copy(
                templateJson = templateJson,
                intervalType = interval.name,
                nextDate = nextDate,
                autoAdd = autoAdd,
            ))
        }

    fun toggleAutoAdd(rule: RecurringRuleEntity) = viewModelScope.launch {
        dao.upsert(rule.copy(autoAdd = !rule.autoAdd))
    }

    fun toggleEnabled(rule: RecurringRuleEntity) = viewModelScope.launch {
        dao.upsert(rule.copy(enabled = !rule.enabled))
    }

    fun delete(id: Long) = viewModelScope.launch { dao.delete(id) }
}
