package com.financeapp.core.domain.model

enum class TransactionType { INCOME, EXPENSE }
enum class CategoryType { INCOME, EXPENSE, BOTH }
enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY }
enum class RepeatType { NONE, WEEKLY, MONTHLY, YEARLY }
enum class IntervalType { DAILY, WEEKLY, MONTHLY, YEARLY }
enum class Currency(val code: String, val symbol: String) {
    RUB("RUB", "₽"),
    USD("USD", "$"),
    EUR("EUR", "€"),
}
enum class ThemeMode { SYSTEM, LIGHT, DARK }
// NOTE: PURPLE = green accent (default), ORANGE = amber alt — names kept stable to limit churn.
enum class ColorScheme { PURPLE, ORANGE, INDIGO, TERRACOTTA }
enum class AppLanguage(val tag: String) { RU("ru"), EN("en") }
/** How soon the app re-locks after leaving the foreground. */
enum class AutoLock(val millis: Long) { IMMEDIATE(0L), ONE_MIN(60_000L), FIVE_MIN(300_000L) }
