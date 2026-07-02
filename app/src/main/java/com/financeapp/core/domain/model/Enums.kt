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
enum class ColorScheme { PURPLE, ORANGE }
enum class AppLanguage(val tag: String) { RU("ru"), EN("en") }
