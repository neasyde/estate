package com.financeapp.core.domain.usecase

import com.financeapp.core.config.AppConfig
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RatesRefreshPolicyTest {
    private fun cfg(key: String) = object : AppConfig { override val defaultExchangeApiKey = key }

    @Test fun userKeyWins() {
        assertThat(resolveApiKey("user", cfg("default"))).isEqualTo("user")
    }

    @Test fun fallsBackToConfig() {
        assertThat(resolveApiKey(null, cfg("default"))).isEqualTo("default")
        assertThat(resolveApiKey("   ", cfg("default"))).isEqualTo("default")
    }

    @Test fun nullWhenNoneAvailable() {
        assertThat(resolveApiKey(null, cfg(""))).isNull()
        assertThat(resolveApiKey("  ", cfg("   "))).isNull()
    }

    @Test fun refreshWhenStaleAndHasKey() {
        assertThat(shouldRefreshRates(now = 100_000, updatedAt = 0, intervalMs = 1000, hasKey = true)).isTrue()
    }

    @Test fun noRefreshWhenFresh() {
        assertThat(shouldRefreshRates(now = 1500, updatedAt = 1000, intervalMs = 1000, hasKey = true)).isFalse()
    }

    @Test fun noRefreshWithoutKey() {
        assertThat(shouldRefreshRates(now = 100_000, updatedAt = 0, intervalMs = 1000, hasKey = false)).isFalse()
    }
}
