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
    const val RECURRING = "recurring"
    const val DONATIONS = "donations"
    const val MORE_HUB = "more_hub"

    val bottomBarRoutes = setOf(DASHBOARD, TRANSACTIONS, BUDGETS, ANALYTICS, MORE_HUB)
}
