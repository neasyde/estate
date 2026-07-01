package com.financeapp.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId

class DateUtilsTest {
    private val z = ZoneId.of("UTC")
    private val noonJan15 = 1736942400000L // 2025-01-15T12:00:00Z

    @Test fun startOfDayZeroesTime() {
        val sod = DateUtils.startOfDay(noonJan15, z)
        assertThat(sod).isEqualTo(1736899200000L) // 2025-01-15T00:00Z
    }

    @Test fun last7DayStartsHas7Ascending() {
        val days = DateUtils.lastNDayStarts(noonJan15, 7, z)
        assertThat(days).hasSize(7)
        assertThat(days.last()).isEqualTo(DateUtils.startOfDay(noonJan15, z))
        assertThat(days).isInOrder()
    }

    @Test fun dayLabelToday() {
        assertThat(DateUtils.dayLabel(DateUtils.startOfDay(noonJan15, z), noonJan15, z))
            .isEqualTo(DateUtils.DayLabel.Today)
    }
}
