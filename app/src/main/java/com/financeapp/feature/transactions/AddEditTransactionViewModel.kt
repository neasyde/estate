package com.financeapp.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.RecurringTemplate
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.domain.usecase.AddRecurringRuleUseCase
import com.financeapp.core.domain.usecase.ObserveCategoriesUseCase
import com.financeapp.core.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class FormState(
    val id: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val currency: Currency = Currency.RUB,
    val categoryId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val recurring: Boolean = false,
    val interval: IntervalType = IntervalType.MONTHLY,
    val autoAdd: Boolean = false,
)

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val save: SaveTransactionUseCase,
    private val addRecurring: AddRecurringRuleUseCase,
    private val txRepo: TransactionRepository,
    observeCats: ObserveCategoriesUseCase,
) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    private val allCategories: StateFlow<List<Category>> = observeCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesForType: StateFlow<List<Category>> = combine(_form, allCategories) { f, cats ->
        val want = if (f.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        cats.filter { it.type == want || it.type == CategoryType.BOTH }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Loads an existing transaction for editing, or resets to a new-transaction form. */
    suspend fun load(id: Long?, defaultCurrency: Currency, presetType: TransactionType?) {
        if (id == null) {
            _form.value = FormState(currency = defaultCurrency, type = presetType ?: TransactionType.EXPENSE)
            return
        }
        val t = txRepo.getById(id) ?: return
        _form.value = FormState(
            id = t.id, type = t.type, amount = t.amount.toString(), currency = t.currency,
            categoryId = t.categoryId, note = t.note.orEmpty(), date = t.date,
        )
    }

    fun setType(t: TransactionType) = _form.update { it.copy(type = t, categoryId = null) }
    fun setAmount(v: String) = _form.update {
        // Only allow digits and at most one decimal point.
        val filtered = v.filter { c -> c.isDigit() || c == '.' }
        val firstDot = filtered.indexOf('.')
        it.copy(amount = if (firstDot == -1) filtered else filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", ""))
    }
    fun setCurrency(c: Currency) = _form.update { it.copy(currency = c) }
    fun setCategory(id: Long) = _form.update { it.copy(categoryId = id) }
    fun setNote(v: String) = _form.update { it.copy(note = v) }

    /** [pickedUtcMillis] comes from M3 DatePicker (UTC midnight); keep the existing time-of-day. */
    fun setDate(pickedUtcMillis: Long) = _form.update {
        val zone = ZoneId.systemDefault()
        val pickedDate = Instant.ofEpochMilli(pickedUtcMillis).atZone(ZoneId.of("UTC")).toLocalDate()
        val currentTime = Instant.ofEpochMilli(it.date).atZone(zone).toLocalTime()
        it.copy(date = pickedDate.atTime(currentTime).atZone(zone).toInstant().toEpochMilli())
    }

    fun setTime(hour: Int, minute: Int) = _form.update {
        val zdt = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault())
        it.copy(date = zdt.withHour(hour).withMinute(minute).withSecond(0).withNano(0).toInstant().toEpochMilli())
    }
    fun setRecurring(b: Boolean) = _form.update { it.copy(recurring = b) }
    fun setInterval(i: IntervalType) = _form.update { it.copy(interval = i) }
    fun setAutoAdd(b: Boolean) = _form.update { it.copy(autoAdd = b) }

    /** Returns true on success; false if validation fails (amount > 0 and a category chosen). */
    suspend fun save(): Boolean {
        val f = _form.value
        val amount = f.amount.toDoubleOrNull()
        if (amount == null || amount <= 0.0 || f.categoryId == null) return false
        val tx = Transaction(
            id = f.id ?: 0L, amount = amount, currency = f.currency, type = f.type,
            categoryId = f.categoryId, note = f.note.ifBlank { null }, date = f.date,
        )
        save(tx)
        if (f.recurring) {
            // Pass the transaction id so the use case can find & update the existing rule
            // for the same source transaction (instead of creating a duplicate).
            addRecurring(
                RecurringTemplate(
                    amount = amount,
                    currency = f.currency.name,
                    type = f.type.name,
                    categoryId = f.categoryId,
                    note = f.note.ifBlank { null },
                    linkedTransactionId = tx.id.takeIf { it != 0L },
                ),
                f.interval, f.date, f.autoAdd,
            )
        }
        return true
    }
}
