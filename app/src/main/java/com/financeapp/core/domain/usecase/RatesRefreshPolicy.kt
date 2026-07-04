package com.financeapp.core.domain.usecase

import com.financeapp.core.config.AppConfig

/**
 * Resolves the effective exchangerate-api key: the user-entered key wins; otherwise the
 * build-time default from [AppConfig]; otherwise null (no key available).
 */
fun resolveApiKey(userKey: String?, config: AppConfig): String? {
    val u = userKey?.trim().orEmpty()
    if (u.isNotEmpty()) return u
    val d = config.defaultExchangeApiKey.trim()
    return d.ifEmpty { null }
}

/** True when a silent auto-refresh should run: a key is available and rates are stale. */
fun shouldRefreshRates(now: Long, updatedAt: Long, intervalMs: Long, hasKey: Boolean): Boolean =
    hasKey && (now - updatedAt >= intervalMs)
