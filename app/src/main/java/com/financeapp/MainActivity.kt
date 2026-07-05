package com.financeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.ui.LocalAnimationsEnabled
import com.financeapp.core.ui.LocalHapticsEnabled
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.ui.theme.FinanceTheme
import com.financeapp.navigation.BottomBar
import com.financeapp.navigation.FinanceNavHost
import com.financeapp.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppRootViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val effective = settings ?: AppSettings()

            FinanceTheme(
                themeMode = effective.themeMode,
                colorScheme = effective.colorScheme,
                appFont = effective.appFont,
                fontSize = effective.fontSize,
            ) {
                CompositionLocalProvider(
                    LocalShowDecimals provides effective.showDecimals,
                    LocalAnimationsEnabled provides effective.animationsEnabled,
                    LocalHapticsEnabled provides effective.hapticsEnabled,
                ) {
                    val nav = rememberNavController()
                    val backStack by nav.currentBackStackEntryAsState()
                    val currentRoute = backStack?.destination?.route

                    Scaffold(
                        bottomBar = {
                            if (currentRoute in Routes.bottomBarRoutes) {
                                BottomBar(currentRoute) { route ->
                                    if (route != currentRoute) {
                                        nav.navigate(route) {
                                            popUpTo(Routes.DASHBOARD) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        },
                    ) { padding ->
                        FinanceNavHost(nav, settings, Modifier.padding(padding))
                    }
                }
            }
        }
    }
}
