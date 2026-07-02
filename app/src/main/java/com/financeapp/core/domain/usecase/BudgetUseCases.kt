package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.BudgetProgress
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.repository.BudgetRepository
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.utils.CurrencyConverter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveBudgetsWithSpentUseCase @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    operator fun invoke(now: Long): Flow<List<BudgetProgress>> =
        combine(
            budgetRepo.observeAll(),
            txRepo.observeAll(),
            catRepo.observeAll(),
            settingsRepo.settings,
        ) { budgets, txs, cats, s ->
            val byId = cats.associateBy { it.id }
            budgets.map { b ->
                val start = when (b.period) {
                    BudgetPeriod.WEEKLY -> DateUtils.startOfWeek(now)
                    BudgetPeriod.MONTHLY -> DateUtils.startOfMonth(now)
                    BudgetPeriod.YEARLY -> DateUtils.startOfYear(now)
                }
                val spent = txs
                    .filter { it.type == TransactionType.EXPENSE && it.categoryId == b.categoryId && it.date >= start }
                    .sumOf { CurrencyConverter.convert(it.amount, it.currency, b.currency, s) }
                val fraction = if (b.limitAmount > 0) spent / b.limitAmount else 0.0
                BudgetProgress(b, byId[b.categoryId], spent, fraction)
            }
        }
}

class SaveBudgetUseCase @Inject constructor(private val repo: BudgetRepository) {
    suspend operator fun invoke(b: Budget): Long = repo.upsert(b)
}

class DeleteBudgetUseCase @Inject constructor(private val repo: BudgetRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
