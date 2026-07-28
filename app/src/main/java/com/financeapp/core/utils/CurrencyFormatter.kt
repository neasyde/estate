package com.financeapp.core.utils

import com.financeapp.core.domain.model.Currency
import java.util.Locale

object CurrencyFormatter {
    private val trailingSymbols = setOf("₽")

    fun format(amount: Double, currency: Currency, showDecimals: Boolean = true): String {
        val n = String.format(Locale.US, if (showDecimals) "%,.2f" else "%,.0f", amount)
        val symbol = currency.symbol
        return if (currency.symbol in trailingSymbols) {
            "$n $symbol"
        } else {
            "$symbol$n"
        }
    }
}
