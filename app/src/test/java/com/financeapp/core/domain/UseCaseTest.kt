package com.financeapp.core.domain

import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionFilter
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.domain.usecase.DuplicateTransactionUseCase
import com.financeapp.core.domain.usecase.ObserveTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeTxRepo(
    val items: MutableList<Transaction> = mutableListOf(),
) : TransactionRepository {
    private val flow = MutableStateFlow(items.toList())
    private fun emit() { flow.value = items.toList() }
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

private class FakeCatRepo(private val cats: List<Category>) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> = flowOf(cats)
    override fun observeByType(type: CategoryType): Flow<List<Category>> =
        flowOf(cats.filter { it.type == type || it.type == CategoryType.BOTH })
    override fun observeManagedByType(type: CategoryType): Flow<List<Category>> =
        flowOf(cats.filter { it.type == type || it.type == CategoryType.BOTH })
    override suspend fun getById(id: Long): Category? = cats.find { it.id == id }
    override suspend fun upsert(c: Category): Long = c.id
    override suspend fun setHidden(id: Long, hidden: Boolean) {}
    override suspend fun delete(id: Long) {}
}

class UseCaseTest {
    @Test fun duplicateCopiesWithNowDate() = runTest {
        val repo = FakeTxRepo(
            mutableListOf(
                Transaction(
                    id = 1, amount = 10.0, currency = Currency.RUB, type = TransactionType.EXPENSE,
                    categoryId = null, note = "n", date = 100L, recurringRuleId = null,
                ),
            ),
        )
        val newId = DuplicateTransactionUseCase(repo)(1, now = 999L)
        val copy = repo.items.first { it.id == newId }
        assertThat(copy.date).isEqualTo(999L)
        assertThat(copy.amount).isEqualTo(10.0)
    }

    @Test fun filterByTypeAndQuery() = runTest {
        val txs = listOf(
            Transaction(1, 10.0, Currency.RUB, TransactionType.EXPENSE, 1, "coffee", 100, null),
            Transaction(2, 20.0, Currency.RUB, TransactionType.INCOME, 2, "pay", 200, null),
        )
        val cats = listOf(
            Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE),
            Category(2, "cat_salary", "work", 0, CategoryType.INCOME),
        )
        val uc = ObserveTransactionsUseCase(FakeTxRepo(txs.toMutableList()), FakeCatRepo(cats))
        val res = uc(TransactionFilter(type = TransactionType.EXPENSE)).first()
        assertThat(res.map { it.transaction.id }).containsExactly(1L)
        assertThat(res.first().category?.name).isEqualTo("cat_food")
    }
}
