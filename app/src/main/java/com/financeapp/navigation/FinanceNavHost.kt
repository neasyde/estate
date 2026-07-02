package com.financeapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.financeapp.R
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.ui.anim.Motion
import com.financeapp.feature.budgets.BudgetsScreen
import com.financeapp.feature.categories.CategoriesScreen
import com.financeapp.feature.dashboard.DashboardScreen
import com.financeapp.feature.lock.LockScreen
import com.financeapp.feature.onboarding.OnboardingScreen
import com.financeapp.feature.placeholder.PlaceholderScreen
import com.financeapp.feature.settings.SettingsScreen
import com.financeapp.feature.splash.SplashScreen
import com.financeapp.feature.transactions.TransactionsScreen

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    settings: AppSettings?,
    modifier: Modifier = Modifier,
) {
    val spec = tween<Float>(Motion.Long, easing = Motion.Emphasized)
    val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(Motion.Long, easing = Motion.Emphasized)
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { slideIntoContainer(SlideDirection.Start, slideSpec) + fadeIn(spec) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, slideSpec) + fadeOut(spec) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, slideSpec) + fadeIn(spec) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, slideSpec) + fadeOut(spec) },
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
                onUnlocked = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOCK) { inclusive = true } } },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onSeeAll = { navController.navigate(Routes.TRANSACTIONS) })
        }
        composable(Routes.TRANSACTIONS) { TransactionsScreen() }
        composable(Routes.BUDGETS) { BudgetsScreen() }
        composable(Routes.ANALYTICS) { PlaceholderScreen("bar_chart", stringResource(R.string.ph_analytics)) }
        composable(Routes.SETTINGS) {
            SettingsScreen(onManageCategories = { navController.navigate(Routes.CATEGORIES) })
        }
        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}
