package com.financeapp.core.utils

import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Currency

/**
 * Converts an amount to the base currency. Rates are stored as RUB per 1 unit
 * of the foreign currency, so conversion pivots through RUB.
 */
object CurrencyConverter {
    fun toBase(amount: Double, from: Currency, s: AppSettings): Double =
        convert(amount, from, s.baseCurrency, s)

    /** Converts [amount] from one currency to another, pivoting through RUB. */
    fun convert(amount: Double, from: Currency, to: Currency, s: AppSettings): Double {
        val inRub = when (from) {
            Currency.RUB -> amount
            Currency.USD -> amount * s.rateUsd
            Currency.EUR -> amount * s.rateEur
        }
        return when (to) {
            Currency.RUB -> inRub
            Currency.USD -> inRub / s.rateUsd
            Currency.EUR -> inRub / s.rateEur
        }
    }
}
