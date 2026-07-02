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
import com.financeapp.feature.dashboard.DashboardScreen
import com.financeapp.feature.lock.LockScreen
import com.financeapp.feature.onboarding.OnboardingScreen
import com.financeapp.feature.placeholder.PlaceholderScreen
import com.financeapp.feature.settings.SettingsScreen
import com.financeapp.feature.splash.SplashScreen
import com.financeapp.feature.transactions.TransactionsScreen

private const val ANIM = 300

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    settings: AppSettings?,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(ANIM)) + fadeIn(tween(ANIM)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(ANIM)) + fadeOut(tween(ANIM)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(ANIM)) + fadeIn(tween(ANIM)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(ANIM)) + fadeOut(tween(ANIM)) },
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
        composable(Routes.BUDGETS) { PlaceholderScreen("savings", stringResource(R.string.ph_budgets)) }
        composable(Routes.ANALYTICS) { PlaceholderScreen("bar_chart", stringResource(R.string.ph_analytics)) }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
