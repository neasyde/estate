package com.financeapp.core.utils

import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Currency
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurrencyConverterTest {
    private val s = AppSettings(baseCurrency = Currency.RUB, rateUsd = 90.0, rateEur = 100.0)

    @Test fun sameCurrencyIsIdentity() {
        assertThat(CurrencyConverter.toBase(100.0, Currency.RUB, s)).isWithin(0.001).of(100.0)
    }

    @Test fun usdToRub() {
        assertThat(CurrencyConverter.toBase(2.0, Currency.USD, s)).isWithin(0.001).of(180.0)
    }

    @Test fun rubToUsdBase() {
        val us = s.copy(baseCurrency = Currency.USD)
        assertThat(CurrencyConverter.toBase(180.0, Currency.RUB, us)).isWithin(0.001).of(2.0)
    }
}
