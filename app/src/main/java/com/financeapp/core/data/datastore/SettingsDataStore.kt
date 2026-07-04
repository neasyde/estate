package com.financeapp.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    val EXCHANGE_API_KEY = stringPreferencesKey("exchange_api_key")
    val RATES_UPDATED_AT = longPreferencesKey("rates_updated_at")
    val AUTO_REFRESH = booleanPreferencesKey("auto_refresh_rates")
    val RATES_INTERVAL = intPreferencesKey("rates_interval_hours")
    val SHOW_DECIMALS = booleanPreferencesKey("show_decimals")
    val DEFAULT_TX_TYPE = stringPreferencesKey("default_tx_type")
    val ANIMATIONS = booleanPreferencesKey("animations_enabled")
    val HAPTICS = booleanPreferencesKey("haptics_enabled")
    val HIDE_BALANCE = booleanPreferencesKey("hide_balance_default")
    val REQUIRE_PIN = booleanPreferencesKey("require_pin_on_launch")
    val AUTO_BIOMETRIC = booleanPreferencesKey("auto_biometric")
    val AUTO_LOCK = stringPreferencesKey("auto_lock")
    val REMINDER_HOUR = intPreferencesKey("default_reminder_hour")
    val REMINDER_LEAD_DAYS = intPreferencesKey("default_reminder_lead_days")
}

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
