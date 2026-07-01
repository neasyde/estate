package com.financeapp.core.utils

import com.financeapp.core.domain.model.Currency
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currency: Currency): String {
        val n = String.format(Locale.US, "%,.2f", amount)
        return when (currency) {
            Currency.RUB -> "$n ₽"
            Currency.USD -> "$$n"
            Currency.EUR -> "€$n"
        }
    }
}
