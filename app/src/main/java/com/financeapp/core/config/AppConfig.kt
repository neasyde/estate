package com.financeapp.core.config

/**
 * App-level build configuration exposed behind an interface so it can be faked in unit
 * tests (the real implementation reads [com.financeapp.BuildConfig]).
 */
interface AppConfig {
    /** Default exchangerate-api.com v6 key baked in at build time (may be empty). */
    val defaultExchangeApiKey: String
}
