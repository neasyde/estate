package com.financeapp.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.core.data.local.dao.BudgetDao
import com.financeapp.core.data.local.dao.CategoryDao
import com.financeapp.core.data.local.dao.RecurringRuleDao
import com.financeapp.core.data.local.dao.ReminderDao
import com.financeapp.core.data.local.dao.TransactionDao
import com.financeapp.core.data.local.entity.BudgetEntity
import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import com.financeapp.core.data.local.entity.ReminderEntity
import com.financeapp.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        ReminderEntity::class,
        RecurringRuleEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun reminderDao(): ReminderDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        const val NAME = "finance.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun build(context: Context, scope: CoroutineScope): FinanceDatabase {
            var instance: FinanceDatabase? = null
            instance = Room.databaseBuilder(context, FinanceDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        scope.launch(Dispatchers.IO) {
                            instance?.categoryDao()?.insertAll(DatabaseSeed.categories())
                        }
                    }
                })
                .build()
            return instance
        }
    }
}
