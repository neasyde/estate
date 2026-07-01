package com.financeapp.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object SettingsKeys {
    val BASE_CURRENCY = stringPreferencesKey("base_currency")
    val RATE_USD = doublePreferencesKey("exchange_rate_usd")
    val RATE_EUR = doublePreferencesKey("exchange_rate_eur")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val COLOR_SCHEME = stringPreferencesKey("color_scheme")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
    val LANGUAGE = stringPreferencesKey("language")
    val ONBOARDING = booleanPreferencesKey("onboarding_completed")
}

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
