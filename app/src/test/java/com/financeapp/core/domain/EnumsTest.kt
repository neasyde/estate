package com.financeapp.core.domain

import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnumsTest {
    @Test fun currencyHasCodeAndSymbol() {
        assertThat(Currency.RUB.symbol).isEqualTo("₽")
        assertThat(Currency.valueOf("USD").code).isEqualTo("USD")
    }

    @Test fun defaultSettingsAreSane() {
        val s = AppSettings()
        assertThat(s.baseCurrency).isEqualTo(Currency.RUB)
        assertThat(s.themeMode).isEqualTo(ThemeMode.SYSTEM)
        assertThat(s.onboardingCompleted).isFalse()
    }
}
