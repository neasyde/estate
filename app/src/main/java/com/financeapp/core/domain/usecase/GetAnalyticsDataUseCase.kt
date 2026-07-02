package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.AnalyticsData
import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.CategorySlice
import com.financeapp.core.domain.model.MonthTotals
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.utils.CurrencyConverter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAnalyticsDataUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    operator fun invoke(now: Long, period: AnalyticsPeriod): Flow<AnalyticsData> =
        combine(txRepo.observeAll(), catRepo.observeAll(), settingsRepo.settings) { txs, cats, s ->
            val byId = cats.associateBy { it.id }
            fun base(t: Transaction) = CurrencyConverter.toBase(t.amount, t.currency, s)

            val start = when (period) {
                AnalyticsPeriod.MONTH -> DateUtils.startOfMonth(now)
                AnalyticsPeriod.YEAR -> DateUtils.startOfYear(now)
                AnalyticsPeriod.ALL -> Long.MIN_VALUE
            }
            val inPeriod = txs.filter { it.date in start..now }
            val income = inPeriod.filter { it.type == TransactionType.INCOME }.sumOf { base(it) }
            val expense = inPeriod.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) }

            val slices = inPeriod
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .map { (catId, list) ->
                    val amount = list.sumOf { base(it) }
                    CategorySlice(catId?.let(byId::get), amount, if (expense > 0) amount / expense else 0.0)
                }
                .sortedByDescending { it.amount }

            val months = DateUtils.lastNMonthStarts(now, 6).map { m ->
                val next = DateUtils.startOfNextMonth(m)
                val monthTx = txs.filter { it.date in m until next }
                MonthTotals(
                    monthStart = m,
                    income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { base(it) },
                    expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) },
                )
            }

            AnalyticsData(income, expense, slices, months)
        }
}
