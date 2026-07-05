package com.financeapp.core.domain

import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.usecase.GetAnalyticsDataUseCase
import com.financeapp.core.utils.DateUtils
import com.financeapp.testutil.FakeCategoryRepository
import com.financeapp.testutil.FakeSettingsRepository
import com.financeapp.testutil.FakeTransactionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AnalyticsUseCaseTest {
    @Test fun groupsExpensesByCategoryWithConvertedFractions() = runTest {
        val now = System.currentTimeMillis()
        val settings = AppSettings(baseCurrency = Currency.RUB, rateUsd = 90.0, rateEur = 100.0)
        val food = Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE)
        val transport = Category(2, "cat_transport", "directions_car", 0, CategoryType.EXPENSE)
        val txs = mutableListOf(
            Transaction(1, 100.0, Currency.RUB, TransactionType.EXPENSE, 1, null, now), // food 100
            Transaction(2, 1.0, Currency.USD, TransactionType.EXPENSE, 1, null, now), // food +90 => 190
            Transaction(3, 10.0, Currency.RUB, TransactionType.EXPENSE, 2, null, now), // transport 10
            Transaction(4, 500.0, Currency.RUB, TransactionType.INCOME, null, null, now), // income
        )
        val uc = GetAnalyticsDataUseCase(
            FakeTransactionRepository(txs),
            FakeCategoryRepository(listOf(food, transport)),
            FakeSettingsRepository(settings),
        )

        val d = uc(now, AnalyticsPeriod.MONTH, rolling = false).first()

        assertThat(d.income).isWithin(0.001).of(500.0)
        assertThat(d.expense).isWithin(0.001).of(200.0)
        assertThat(d.slices).hasSize(2)
        // Sorted by amount descending: food (190) before transport (10).
        assertThat(d.slices[0].category?.name).isEqualTo("cat_food")
        assertThat(d.slices[0].amount).isWithin(0.001).of(190.0)
        assertThat(d.slices[0].fraction).isWithin(0.001).of(0.95)
        assertThat(d.slices[1].amount).isWithin(0.001).of(10.0)
        // Month period => five weekly trend buckets; the current week holds this data.
        assertThat(d.trend).hasSize(5)
        assertThat(d.trend.last().income).isWithin(0.001).of(500.0)
        assertThat(d.trend.last().expense).isWithin(0.001).of(200.0)
    }

    @Test fun calendarMonthExcludesTransactionsBeforeThisMonth() = runTest {
        val now = System.currentTimeMillis()
        val beforeMonth = DateUtils.startOfMonth(now) - 1_000L
        val settings = AppSettings(baseCurrency = Currency.RUB)
        val food = Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE)
        val txs = mutableListOf(
            Transaction(1, 300.0, Currency.RUB, TransactionType.EXPENSE, 1, null, beforeMonth),
        )
        val uc = GetAnalyticsDataUseCase(
            FakeTransactionRepository(txs),
            FakeCategoryRepository(listOf(food)),
            FakeSettingsRepository(settings),
        )

        assertThat(uc(now, AnalyticsPeriod.MONTH, rolling = false).first().expense).isWithin(0.001).of(0.0)
        // A rolling year still reaches back far enough to include it.
        assertThat(uc(now, AnalyticsPeriod.YEAR, rolling = true).first().expense).isWithin(0.001).of(300.0)
    }
}
