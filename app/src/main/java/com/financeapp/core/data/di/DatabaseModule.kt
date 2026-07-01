package com.financeapp.core.data.di

import android.content.Context
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.data.local.dao.BudgetDao
import com.financeapp.core.data.local.dao.CategoryDao
import com.financeapp.core.data.local.dao.RecurringRuleDao
import com.financeapp.core.data.local.dao.ReminderDao
import com.financeapp.core.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun db(@ApplicationContext c: Context, @AppScope scope: CoroutineScope): FinanceDatabase =
        FinanceDatabase.build(c, scope)

    @Provides fun txDao(db: FinanceDatabase): TransactionDao = db.transactionDao()
    @Provides fun catDao(db: FinanceDatabase): CategoryDao = db.categoryDao()
    @Provides fun budgetDao(db: FinanceDatabase): BudgetDao = db.budgetDao()
    @Provides fun reminderDao(db: FinanceDatabase): ReminderDao = db.reminderDao()
    @Provides fun recurringDao(db: FinanceDatabase): RecurringRuleDao = db.recurringRuleDao()
}
