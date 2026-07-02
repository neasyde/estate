package com.financeapp.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    sealed interface DayLabel {
        data object Today : DayLabel
        data object Yesterday : DayLabel
        data class Other(val text: String) : DayLabel
    }

    private fun date(millis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun startOfDay(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfMonth(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfWeek(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfYear(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun lastNDayStarts(nowMillis: Long, n: Int, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        val today = date(nowMillis, zone)
        return (n - 1 downTo 0).map {
            today.minusDays(it.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    fun startOfNextMonth(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun lastNMonthStarts(nowMillis: Long, n: Int, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        val firstOfThisMonth = date(nowMillis, zone).withDayOfMonth(1)
        return (n - 1 downTo 0).map {
            firstOfThisMonth.minusMonths(it.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    fun monthLabel(monthStart: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        date(monthStart, zone).format(DateTimeFormatter.ofPattern("LLL", Locale.getDefault()))

    fun dayLabel(dayStart: Long, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): DayLabel {
        val d = date(dayStart, zone)
        val today = date(nowMillis, zone)
        return when (d) {
            today -> DayLabel.Today
            today.minusDays(1) -> DayLabel.Yesterday
            else -> DayLabel.Other(d.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())))
        }
    }
}
