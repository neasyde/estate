package com.financeapp

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.ui.LocalAnimationsEnabled
import com.financeapp.core.ui.LocalHapticsEnabled
import com.financeapp.core.ui.LocalShowDecimals
import com.financeapp.core.ui.theme.FinanceTheme
import com.financeapp.core.utils.DateUtils
import com.financeapp.core.utils.LocaleManager
import com.financeapp.navigation.BottomBar
import com.financeapp.navigation.FinanceNavHost
import com.financeapp.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_LANGUAGE_TAG = "language_tag"
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val langTag = prefs.getString(KEY_LANGUAGE_TAG, null)
        if (langTag != null) {
            val locale = Locale(langTag)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                config.setLocales(android.os.LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.setLocale(locale)
            }
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppRootViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val deviceLang = remember { LocaleManager.deviceLanguage() }
            val effective = settings ?: AppSettings().copy(language = deviceLang)
            val baseContext = LocalContext.current
            val localeContext = LocaleManager.contextWithLocale(baseContext, effective.language)

            LaunchedEffect(effective.language) {
                DateUtils.setLocale(Locale(effective.language.tag))
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(KEY_LANGUAGE_TAG, effective.language.tag)
                    .apply()
            }

            // --- Auto-lock: track background time ---
            var lastBackground by rememberSaveable { mutableLongStateOf(0L) }
            val nav = rememberNavController()
            val backStack by nav.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            // Store currentRoute in a ref so the lifecycle observer always reads the latest value
            val currentRouteRef = rememberSaveable { mutableStateOf<String?>(null) }
            currentRouteRef.value = currentRoute

            // Use rememberUpdatedState so the lifecycle observer always reads the latest effective settings
            val currentEffective by rememberUpdatedState(effective)

            // Incremented on every ON_START to trigger auto-theme recomputation
            var resumeTick by remember { mutableIntStateOf(0) }

            // --- Auto-switch theme: recompute on settings change OR on app resume ---
            var effectiveThemeMode by remember { mutableStateOf(effective.themeMode) }
            LaunchedEffect(effective.themeMode, effective.autoSwitchTheme, effective.autoSwitchStart, effective.autoSwitchEnd, resumeTick) {
                val e = currentEffective
                effectiveThemeMode = if (e.autoSwitchTheme) {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val start = e.autoSwitchStart
                    val end = e.autoSwitchEnd
                    val inDarkWindow = if (start <= end) {
                        hour in start until end
                    } else {
                        hour >= start || hour < end
                    }
                    if (inDarkWindow) ThemeMode.DARK else ThemeMode.LIGHT
                } else {
                    e.themeMode
                }
            }

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            lastBackground = SystemClock.elapsedRealtime()
                        }
                        Lifecycle.Event.ON_START -> {
                            // Trigger auto-theme recomputation via LaunchedEffect
                            resumeTick++

                            // Auto-lock check using current settings
                            val e = currentEffective
                            val elapsed = if (lastBackground > 0) SystemClock.elapsedRealtime() - lastBackground else 0L
                            val shouldLock = e.requirePinOnLaunch && e.pinHash != null && when (e.autoLock) {
                                AutoLock.IMMEDIATE -> lastBackground > 0 && elapsed > 500L
                                AutoLock.ONE_MIN -> lastBackground > 0 && elapsed > AutoLock.ONE_MIN.millis
                                AutoLock.FIVE_MIN -> lastBackground > 0 && elapsed > AutoLock.FIVE_MIN.millis
                            }
                            val route = currentRouteRef.value
                            if (shouldLock && route != Routes.LOCK && route != Routes.SPLASH && route != Routes.ONBOARDING) {
                                lastBackground = 0L
                                nav.navigate(Routes.LOCK) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                        else -> {}
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            CompositionLocalProvider(LocalContext provides localeContext) {
                FinanceTheme(
                    themeMode = effectiveThemeMode,
                    colorScheme = effective.colorScheme,
                    appFont = effective.appFont,
                    fontSize = effective.fontSize,
                    amoledMode = effective.amoledMode,
                ) {
                    CompositionLocalProvider(
                        LocalShowDecimals provides effective.showDecimals,
                        LocalAnimationsEnabled provides effective.animationsEnabled,
                        LocalHapticsEnabled provides effective.hapticsEnabled,
                    ) {
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
}
