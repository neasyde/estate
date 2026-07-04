package com.financeapp.core.utils

import com.financeapp.core.domain.model.Currency
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currency: Currency, showDecimals: Boolean = true): String {
        val n = String.format(Locale.US, if (showDecimals) "%,.2f" else "%,.0f", amount)
        return when (currency) {
            Currency.RUB -> "$n ₽"
            Currency.USD -> "$$n"
            Currency.EUR -> "€$n"
        }
    }
}
