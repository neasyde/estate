package com.financeapp.core.data.di

import com.financeapp.core.data.repository.BudgetRepositoryImpl
import com.financeapp.core.data.repository.CategoryRepositoryImpl
import com.financeapp.core.data.repository.RecurringRuleRepositoryImpl
import com.financeapp.core.data.repository.ReminderRepositoryImpl
import com.financeapp.core.data.repository.SettingsRepositoryImpl
import com.financeapp.core.data.repository.TransactionRepositoryImpl
import com.financeapp.core.domain.repository.BudgetRepository
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.RecurringRuleRepository
import com.financeapp.core.domain.repository.ReminderRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun transactions(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun categories(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun recurringRules(impl: RecurringRuleRepositoryImpl): RecurringRuleRepository

    @Binds @Singleton
    abstract fun budgets(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds @Singleton
    abstract fun reminders(impl: ReminderRepositoryImpl): ReminderRepository

}
