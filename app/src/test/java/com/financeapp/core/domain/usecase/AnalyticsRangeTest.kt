package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.AnalyticsPeriod
import com.financeapp.core.utils.DateUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalyticsRangeTest {
    private val now = 1_720_000_000_000L // fixed instant
    private val day = 86_400_000L

    @Test fun allIsAllTime() {
        assertThat(analyticsStart(now, AnalyticsPeriod.ALL, rolling = true)).isEqualTo(Long.MIN_VALUE)
        assertThat(analyticsStart(now, AnalyticsPeriod.ALL, rolling = false)).isEqualTo(Long.MIN_VALUE)
    }

    @Test fun rollingWindows() {
        assertThat(analyticsStart(now, AnalyticsPeriod.WEEK, rolling = true)).isEqualTo(now - 7 * day)
        assertThat(analyticsStart(now, AnalyticsPeriod.MONTH, rolling = true)).isEqualTo(now - 30 * day)
        assertThat(analyticsStart(now, AnalyticsPeriod.YEAR, rolling = true)).isEqualTo(now - 365 * day)
    }

    @Test fun calendarWindows() {
        assertThat(analyticsStart(now, AnalyticsPeriod.WEEK, rolling = false)).isEqualTo(DateUtils.startOfWeek(now))
        assertThat(analyticsStart(now, AnalyticsPeriod.MONTH, rolling = false)).isEqualTo(DateUtils.startOfMonth(now))
        assertThat(analyticsStart(now, AnalyticsPeriod.YEAR, rolling = false)).isEqualTo(DateUtils.startOfYear(now))
    }
}
