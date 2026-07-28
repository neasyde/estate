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
    version = 12,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN notifyMinuteOfDay INTEGER NOT NULL DEFAULT 540")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE categories SET sortOrder = id")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        currentAmount REAL NOT NULL DEFAULT 0.0,
                        icon TEXT NOT NULL,
                        deadline INTEGER,
                        color INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS goal_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        goalId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        type TEXT NOT NULL
                    )
                    """
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL DEFAULT 0,
                        targetAmount REAL,
                        deadline INTEGER,
                        createdAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS project_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        transactionId INTEGER NOT NULL
                    )
                    """
                )
                db.execSQL("ALTER TABLE transactions ADD COLUMN projectId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS achievements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        key TEXT NOT NULL,
                        unlockedAt INTEGER,
                        progress INTEGER NOT NULL DEFAULT 0,
                        target INTEGER NOT NULL
                    )
                    """
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS quick_actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        label TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        targetId INTEGER,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        isVisible INTEGER NOT NULL DEFAULT 1
                    )
                    """
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN enabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN currency TEXT NOT NULL DEFAULT 'RUB'")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS savings_goals")
                db.execSQL("DROP TABLE IF EXISTS goal_transactions")
                db.execSQL("DROP TABLE IF EXISTS projects")
                db.execSQL("DROP TABLE IF EXISTS project_transactions")
                db.execSQL("DROP TABLE IF EXISTS quick_actions")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN templateTransactionId INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_templateTransactionId ON recurring_rules(templateTransactionId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS achievements")
            }
        }

        fun build(context: Context, scope: CoroutineScope): FinanceDatabase {
            lateinit var instance: FinanceDatabase
            instance = Room.databaseBuilder(context, FinanceDatabase::class.java, NAME)
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
                    MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
                    MIGRATION_11_12,
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        scope.launch(Dispatchers.IO) {
                            instance.categoryDao().insertAll(DatabaseSeed.categories())
                        }
                    }
                })
                .build()
            return instance
        }
    }
}
