package com.financeapp.core.utils

import com.financeapp.core.domain.model.Currency
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurrencyFormatterTest {
    @Test fun formatsRub() {
        assertThat(CurrencyFormatter.format(1234.5, Currency.RUB)).isEqualTo("1,234.50 ₽")
    }

    @Test fun formatsUsd() {
        assertThat(CurrencyFormatter.format(1234.5, Currency.USD)).isEqualTo("$1,234.50")
    }

    @Test fun formatsEur() {
        assertThat(CurrencyFormatter.format(9.9, Currency.EUR)).isEqualTo("€9.90")
    }
}
