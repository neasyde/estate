package com.financeapp.feature

import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.usecase.AddRecurringRuleUseCase
import com.financeapp.core.domain.usecase.ObserveCategoriesUseCase
import com.financeapp.core.domain.usecase.SaveTransactionUseCase
import com.financeapp.feature.transactions.AddEditTransactionViewModel
import com.financeapp.testutil.FakeCategoryRepository
import com.financeapp.testutil.FakeRecurringRuleRepository
import com.financeapp.testutil.FakeTransactionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTransactionViewModelTest {
    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(txRepo: FakeTransactionRepository): AddEditTransactionViewModel {
        val cats = FakeCategoryRepository(
            listOf(Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE)),
        )
        return AddEditTransactionViewModel(
            SaveTransactionUseCase(txRepo),
            AddRecurringRuleUseCase(FakeRecurringRuleRepository()),
            txRepo,
            ObserveCategoriesUseCase(cats),
        )
    }

    @Test fun savesValidTransaction() = runTest {
        val txRepo = FakeTransactionRepository()
        val vm = vm(txRepo)
        vm.load(null, Currency.RUB, TransactionType.EXPENSE)
        vm.setAmount("12.5")
        vm.setCategory(1)
        assertThat(vm.save()).isTrue()
        assertThat(txRepo.all()).hasSize(1)
        assertThat(txRepo.all().first().amount).isEqualTo(12.5)
    }

    @Test fun rejectsZeroAmountOrNoCategory() = runTest {
        val txRepo = FakeTransactionRepository()
        val vm = vm(txRepo)
        vm.load(null, Currency.RUB, TransactionType.EXPENSE)
        vm.setAmount("0")
        vm.setCategory(1)
        assertThat(vm.save()).isFalse()
        vm.setAmount("10")
        // reset category to none by reloading a fresh form
        vm.load(null, Currency.RUB, TransactionType.EXPENSE)
        vm.setAmount("10")
        assertThat(vm.save()).isFalse() // no category chosen
        assertThat(txRepo.all()).isEmpty()
    }
}
