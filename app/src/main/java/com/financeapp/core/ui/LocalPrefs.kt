package com.financeapp.core.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-preference CompositionLocals, provided once at the root from [AppSettings] so any
 * composable can honor them without threading the whole settings object through.
 * Defaults keep behavior unchanged when a provider is absent (e.g. previews/tests).
 */
val LocalShowDecimals = staticCompositionLocalOf { true }
val LocalAnimationsEnabled = staticCompositionLocalOf { true }
val LocalHapticsEnabled = staticCompositionLocalOf { true }
