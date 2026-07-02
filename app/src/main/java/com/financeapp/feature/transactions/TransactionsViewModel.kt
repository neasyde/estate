package com.financeapp.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.TransactionFilter
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.usecase.DeleteTransactionUseCase
import com.financeapp.core.domain.usecase.DuplicateTransactionUseCase
import com.financeapp.core.domain.usecase.ObserveCategoriesUseCase
import com.financeapp.core.domain.usecase.ObserveTransactionsUseCase
import com.financeapp.core.domain.usecase.SaveTransactionUseCase
import com.financeapp.core.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    observeTx: ObserveTransactionsUseCase,
    observeCats: ObserveCategoriesUseCase,
    private val deleteTx: DeleteTransactionUseCase,
    private val duplicateTx: DuplicateTransactionUseCase,
    private val saveTx: SaveTransactionUseCase,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    private var lastDeleted: Transaction? = null
    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    /** Transactions grouped by day-start, days ordered newest first. */
    val grouped: StateFlow<Map<Long, List<TransactionWithCategory>>> = _filter
        .flatMapLatest { observeTx(it) }
        .map { list ->
            list.groupBy { DateUtils.startOfDay(it.transaction.date) }
                .toSortedMap(compareByDescending { it })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categories: StateFlow<List<Category>> = observeCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency: StateFlow<Currency> = settingsRepo.settings
        .map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.RUB)

    fun setTypeFilter(t: TransactionType?) = _filter.update { it.copy(type = t) }
    fun setCategoryFilter(id: Long?) = _filter.update { it.copy(categoryId = id) }
    fun setCurrencyFilter(c: Currency?) = _filter.update { it.copy(currency = c) }
    fun setPeriod(start: Long?, end: Long?) = _filter.update { it.copy(start = start, end = end) }
    fun setQuery(q: String) = _filter.update { it.copy(query = q) }

    fun delete(tx: Transaction) {
        lastDeleted = tx
        viewModelScope.launch { deleteTx(tx.id) }
    }

    fun undoDelete() {
        val d = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { saveTx(d) }
    }

    fun duplicate(id: Long) = viewModelScope.launch { duplicateTx(id, System.currentTimeMillis()) }
}
