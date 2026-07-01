package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.TransactionFilter
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveTransactionsUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
) {
    operator fun invoke(filter: TransactionFilter): Flow<List<TransactionWithCategory>> =
        combine(txRepo.observeAll(), catRepo.observeAll()) { txs, cats ->
            val byId = cats.associateBy { it.id }
            txs.asSequence()
                .filter { filter.type == null || it.type == filter.type }
                .filter { filter.categoryId == null || it.categoryId == filter.categoryId }
                .filter { filter.currency == null || it.currency == filter.currency }
                .filter { filter.start == null || it.date >= filter.start }
                .filter { filter.end == null || it.date < filter.end }
                .map { TransactionWithCategory(it, it.categoryId?.let(byId::get)) }
                .filter { twc ->
                    filter.query.isBlank() ||
                        twc.transaction.note?.contains(filter.query, ignoreCase = true) == true ||
                        twc.category?.name?.contains(filter.query, ignoreCase = true) == true
                }
                .toList()
        }
}
