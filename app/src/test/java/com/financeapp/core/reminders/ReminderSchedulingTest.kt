package com.financeapp.core.reminders

import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderSchedulingTest {
    private val day = 86_400_000L

    @Test fun futureOneOffFiresAroundDueDate() {
        val now = System.currentTimeMillis()
        val due = now + 30 * day
        val t = ReminderIntent.triggerAt(
            Reminder(1, "x", dueDate = due, notifyDaysBefore = 0, repeat = RepeatType.NONE),
        )
        assertThat(t).isNotNull()
        assertThat(t!!).isGreaterThan(now)
        assertThat(t).isLessThan(due + day)
    }

    @Test fun notifyDaysBeforeMovesTriggerEarlier() {
        val due = System.currentTimeMillis() + 60 * day
        val t0 = ReminderIntent.triggerAt(Reminder(1, "x", dueDate = due, notifyDaysBefore = 0, repeat = RepeatType.NONE))!!
        val t3 = ReminderIntent.triggerAt(Reminder(1, "x", dueDate = due, notifyDaysBefore = 3, repeat = RepeatType.NONE))!!
        assertThat(t3).isLessThan(t0)
    }

    @Test fun pastOneOffIsNotScheduled() {
        val due = System.currentTimeMillis() - 30 * day
        val t = ReminderIntent.triggerAt(
            Reminder(1, "x", dueDate = due, notifyDaysBefore = 0, repeat = RepeatType.NONE),
        )
        assertThat(t).isNull()
    }

    @Test fun pastRepeatingAdvancesToFuture() {
        val now = System.currentTimeMillis()
        val due = now - 30 * day
        val t = ReminderIntent.triggerAt(
            Reminder(1, "x", dueDate = due, notifyDaysBefore = 0, repeat = RepeatType.WEEKLY),
        )
        assertThat(t).isNotNull()
        assertThat(t!!).isGreaterThan(now)
    }
}
