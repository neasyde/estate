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

    fun startOfNextDay(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfMonth(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfWeek(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfNextWeek(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfYear(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun lastNDayStarts(nowMillis: Long, n: Int, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        val today = date(nowMillis, zone)
        return (n - 1 downTo 0).map {
            today.minusDays(it.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    fun lastNWeekStarts(nowMillis: Long, n: Int, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        val thisWeek = date(nowMillis, zone)
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        return (n - 1 downTo 0).map {
            thisWeek.minusWeeks(it.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
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

    /** Short weekday name (e.g. "Mon"/"Пн") — used for daily trend labels. */
    fun weekdayLabel(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        date(millis, zone).format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))

    /** Day-of-month number (e.g. "5") — used for weekly trend labels. */
    fun dayOfMonthLabel(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        date(millis, zone).dayOfMonth.toString()

    /** 24-hour clock time (e.g. "09:41") for a transaction timestamp. */
    fun timeLabel(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

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
