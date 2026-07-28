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
        if (!amount.isFinite()) return 0.0
        val inRub = toRub(amount, from, s)
        return fromRub(inRub, to, s)
    }

    private fun toRub(amount: Double, from: Currency, s: AppSettings): Double {
        if (!amount.isFinite()) return 0.0
        return when (from) {
            Currency.RUB -> amount
            Currency.USD -> amount * s.rateUsd
            Currency.EUR -> amount * s.rateEur
            Currency.CNY -> amount * s.rateCny
            Currency.KZT -> amount * s.rateKzt
            else -> {
                val rate = s.customRates[from.code]
                if (rate != null && rate > 0 && rate.isFinite()) amount * rate else amount
            }
        }
    }

    private fun fromRub(inRub: Double, to: Currency, s: AppSettings): Double = when (to) {
        Currency.RUB -> inRub
        Currency.USD -> if (s.rateUsd > 0) inRub / s.rateUsd else inRub
        Currency.EUR -> if (s.rateEur > 0) inRub / s.rateEur else inRub
        Currency.CNY -> if (s.rateCny > 0) inRub / s.rateCny else inRub
        Currency.KZT -> if (s.rateKzt > 0) inRub / s.rateKzt else inRub
        else -> {
            val rate = s.customRates[to.code]
            if (rate != null && rate > 0) inRub / rate else inRub
        }
    }
}
