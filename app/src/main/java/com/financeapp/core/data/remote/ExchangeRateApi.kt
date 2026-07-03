package com.financeapp.core.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a rate refresh. [usdRate]/[eurRate] are RUB per 1 unit of the foreign
 * currency (the convention used by [com.financeapp.core.utils.CurrencyConverter]).
 */
sealed interface ExchangeResult {
    data class Success(val usdRate: Double, val eurRate: Double) : ExchangeResult
    /** [type] mirrors the API's `error-type` (e.g. "invalid-key") or "network"/"parse". */
    data class Failure(val type: String) : ExchangeResult
}

/**
 * Fetches currency rates from exchangerate-api.com v6 using the user's API key.
 * Plain [HttpURLConnection] + org.json — no extra dependencies. HTTPS only.
 */
@Singleton
class ExchangeRateApi @Inject constructor() {

    suspend fun fetchRates(apiKey: String): ExchangeResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) return@withContext ExchangeResult.Failure("no-key")
        // Base RUB: conversion_rates.USD = USD per 1 RUB, so RUB-per-USD = 1 / that.
        val url = URL("https://v6.exchangerate-api.com/v6/$key/latest/RUB")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: return@withContext ExchangeResult.Failure("network")
            val json = JSONObject(body)
            if (json.optString("result") != "success") {
                return@withContext ExchangeResult.Failure(json.optString("error-type", "error"))
            }
            val rates = json.getJSONObject("conversion_rates")
            val usdPerRub = rates.optDouble("USD", 0.0)
            val eurPerRub = rates.optDouble("EUR", 0.0)
            if (usdPerRub <= 0.0 || eurPerRub <= 0.0) return@withContext ExchangeResult.Failure("parse")
            ExchangeResult.Success(usdRate = 1.0 / usdPerRub, eurRate = 1.0 / eurPerRub)
        } catch (e: Exception) {
            ExchangeResult.Failure("network")
        } finally {
            conn.disconnect()
        }
    }
}
