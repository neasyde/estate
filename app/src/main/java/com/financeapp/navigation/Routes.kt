package com.financeapp.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"
    const val REMINDERS = "reminders"

    val bottomBarRoutes = setOf(DASHBOARD, TRANSACTIONS, BUDGETS, ANALYTICS, SETTINGS)
}
