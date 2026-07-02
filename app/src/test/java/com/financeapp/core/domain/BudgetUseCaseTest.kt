package com.financeapp.core.domain

import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Budget
import com.financeapp.core.domain.model.BudgetPeriod
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.usecase.ObserveBudgetsWithSpentUseCase
import com.financeapp.core.utils.DateUtils
import com.financeapp.testutil.FakeBudgetRepository
import com.financeapp.testutil.FakeCategoryRepository
import com.financeapp.testutil.FakeSettingsRepository
import com.financeapp.testutil.FakeTransactionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BudgetUseCaseTest {
    @Test fun computesSpentWithinPeriodAndConvertsCurrency() = runTest {
        val now = System.currentTimeMillis()
        val monthStart = DateUtils.startOfMonth(now)
        val settings = AppSettings(baseCurrency = Currency.RUB, rateUsd = 90.0, rateEur = 100.0)
        val budget = Budget(1, categoryId = 1, limitAmount = 1000.0, currency = Currency.RUB, period = BudgetPeriod.MONTHLY)
        val cat = Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE)
        val txs = mutableListOf(
            Transaction(1, 100.0, Currency.RUB, TransactionType.EXPENSE, 1, null, now, null), // counts: 100
            Transaction(2, 1.0, Currency.USD, TransactionType.EXPENSE, 1, null, now, null), // counts: 90
            Transaction(3, 500.0, Currency.RUB, TransactionType.EXPENSE, 2, null, now, null), // other category
            Transaction(4, 999.0, Currency.RUB, TransactionType.EXPENSE, 1, null, monthStart - 1, null), // before period
            Transaction(5, 50.0, Currency.RUB, TransactionType.INCOME, 1, null, now, null), // income, not expense
        )
        val uc = ObserveBudgetsWithSpentUseCase(
            FakeBudgetRepository(listOf(budget)),
            FakeTransactionRepository(txs),
            FakeCategoryRepository(listOf(cat)),
            FakeSettingsRepository(settings),
        )
        val result = uc(now).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].spent).isWithin(0.001).of(190.0)
        assertThat(result[0].fraction).isWithin(0.001).of(0.19)
        assertThat(result[0].category?.name).isEqualTo("cat_food")
    }
}
