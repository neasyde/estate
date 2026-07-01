package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.DashboardData
import com.financeapp.core.domain.model.DayAmount
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.utils.CurrencyConverter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    operator fun invoke(now: Long): Flow<DashboardData> =
        combine(txRepo.observeAll(), catRepo.observeAll(), settingsRepo.settings) { txs, cats, s ->
            val byId = cats.associateBy { it.id }
            fun base(t: Transaction) = CurrencyConverter.toBase(t.amount, t.currency, s)

            val balance = txs.sumOf { if (it.type == TransactionType.INCOME) base(it) else -base(it) }

            val monthStart = DateUtils.startOfMonth(now)
            val monthTx = txs.filter { it.date in monthStart..now }
            val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { base(it) }
            val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) }

            val series = DateUtils.lastNDayStarts(now, 7).map { d ->
                val next = d + 86_400_000L
                val dayExpense = txs
                    .filter { it.type == TransactionType.EXPENSE && it.date in d until next }
                    .sumOf { base(it) }
                DayAmount(d, dayExpense)
            }

            val recent = txs.sortedByDescending { it.date }.take(5)
                .map { TransactionWithCategory(it, it.categoryId?.let(byId::get)) }

            DashboardData(balance, income, expense, series, recent)
        }
}
