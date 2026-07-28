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
import kotlinx.serialization.json.Json
import javax.inject.Named
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

    /** Default — used for hot-path parsing (recurring rules). Compact output. */
    @Provides
    @Singleton
    @Named("default")
    fun defaultJson(): Json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /** Pretty — used for human-readable backup EXPORT. Slower but the file opens nicely in Notepad. */
    @Provides
    @Singleton
    @Named("pretty")
    fun prettyJson(): Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
}
