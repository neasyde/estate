package com.financeapp.testutil

import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.IntervalType
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.repository.BudgetRepository
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.RecurringRuleRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val _s = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = _s
    fun current() = _s.value
    override suspend fun setBaseCurrency(c: Currency) = _s.update { it.copy(baseCurrency = c) }
    override suspend fun setRates(usd: Double, eur: Double) = _s.update { it.copy(rateUsd = usd, rateEur = eur) }
    override suspend fun setAutoRefreshRates(enabled: Boolean) = _s.update { it.copy(autoRefreshRates = enabled) }
    override suspend fun setRatesIntervalHours(hours: Int) = _s.update { it.copy(ratesIntervalHours = hours) }
    override suspend fun setExchangeApiKey(key: String?) = _s.update { it.copy(exchangeApiKey = key?.trim()?.takeIf(String::isNotEmpty)) }
    override suspend fun setThemeMode(m: ThemeMode) = _s.update { it.copy(themeMode = m) }
    override suspend fun setColorScheme(s: ColorScheme) = _s.update { it.copy(colorScheme = s) }
    override suspend fun setLanguage(l: AppLanguage) = _s.update { it.copy(language = l) }
    override suspend fun setPinHash(hash: String?) = _s.update { it.copy(pinHash = hash) }
    override suspend fun setBiometricEnabled(b: Boolean) = _s.update { it.copy(biometricEnabled = b) }
    override suspend fun setOnboardingCompleted(b: Boolean) = _s.update { it.copy(onboardingCompleted = b) }
}

class FakeTransactionRepository(
    private val items: MutableList<Transaction> = mutableListOf(),
) : TransactionRepository {
    private val flow = MutableStateFlow(items.toList())
    private fun emit() { flow.value = items.toList() }
    fun all(): List<Transaction> = items.toList()
    override fun observeAll(): Flow<List<Transaction>> = flow
    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        flow.map { it.sortedByDescending { t -> t.date }.take(limit) }
    override fun observeBetween(start: Long, end: Long): Flow<List<Transaction>> =
        flow.map { it.filter { t -> t.date in start until end } }
    override suspend fun getById(id: Long): Transaction? = items.find { it.id == id }
    override suspend fun upsert(t: Transaction): Long {
        val id = if (t.id == 0L) (items.maxOfOrNull { it.id } ?: 0L) + 1 else t.id
        items.removeAll { it.id == id }
        items.add(t.copy(id = id))
        emit()
        return id
    }
    override suspend fun delete(id: Long) { items.removeAll { it.id == id }; emit() }
}

class FakeCategoryRepository(private val cats: List<Category> = emptyList()) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> = flowOf(cats)
    override fun observeByType(type: CategoryType): Flow<List<Category>> =
        flowOf(cats.filter { (it.type == type || it.type == CategoryType.BOTH) && !it.isHidden })
    override fun observeManagedByType(type: CategoryType): Flow<List<Category>> =
        flowOf(cats.filter { it.type == type || it.type == CategoryType.BOTH })
    override suspend fun getById(id: Long): Category? = cats.find { it.id == id }
    override suspend fun upsert(c: Category): Long = c.id
    override suspend fun setHidden(id: Long, hidden: Boolean) {}
    override suspend fun delete(id: Long) {}
}

class FakeRecurringRuleRepository : RecurringRuleRepository {
    val added = mutableListOf<String>()
    override suspend fun add(templateJson: String, interval: IntervalType, nextDate: Long, autoAdd: Boolean): Long {
        added.add(templateJson)
        return added.size.toLong()
    }
}

class FakeBudgetRepository(private val budgets: List<Budget> = emptyList()) : BudgetRepository {
    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<Budget>> = flowOf(budgets)
    override suspend fun upsert(b: Budget): Long = b.id
    override suspend fun delete(id: Long) {}
}
