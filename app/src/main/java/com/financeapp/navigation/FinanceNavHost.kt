package com.financeapp.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.ui.anim.Motion
import com.financeapp.feature.analytics.AnalyticsScreen
import com.financeapp.feature.budgets.BudgetsScreen
import com.financeapp.feature.categories.CategoriesScreen
import com.financeapp.feature.dashboard.DashboardScreen
import com.financeapp.feature.lock.LockScreen
import com.financeapp.feature.onboarding.OnboardingScreen
import com.financeapp.feature.reminders.RemindersScreen
import com.financeapp.feature.settings.SettingsScreen
import com.financeapp.feature.splash.SplashScreen
import com.financeapp.feature.transactions.TransactionsScreen

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    settings: AppSettings?,
    modifier: Modifier = Modifier,
) {
    val enter = tween<Float>(Motion.Medium, easing = Motion.Emphasized)
    val exit = tween<Float>(Motion.Short, easing = Motion.Emphasized)
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { fadeIn(enter) + scaleIn(initialScale = 0.98f, animationSpec = enter) },
        exitTransition = { fadeOut(exit) },
        popEnterTransition = { fadeIn(enter) },
        popExitTransition = { fadeOut(exit) + scaleOut(targetScale = 0.98f, animationSpec = exit) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(settings = settings) { dest ->
                navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = { hasPin ->
                val dest = if (hasPin) Routes.LOCK else Routes.DASHBOARD
                navController.navigate(dest) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }
        composable(Routes.LOCK) {
            LockScreen(
                biometricEnabled = settings?.biometricEnabled == true,
                autoBiometric = settings?.autoBiometric != false,
                onUnlocked = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOCK) { inclusive = true } } },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onSeeAll = { navController.navigate(Routes.TRANSACTIONS) })
        }
        composable(Routes.TRANSACTIONS) { TransactionsScreen() }
        composable(Routes.BUDGETS) { BudgetsScreen() }
        composable(Routes.ANALYTICS) { AnalyticsScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                onManageReminders = { navController.navigate(Routes.REMINDERS) },
            )
        }
        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.REMINDERS) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }
    }
}
