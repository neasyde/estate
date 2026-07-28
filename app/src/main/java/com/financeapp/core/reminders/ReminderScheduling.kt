package com.financeapp.core.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType
import com.financeapp.core.utils.CurrencyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Intent plumbing + trigger-time maths shared by the scheduler and the alarm receiver. */
internal object ReminderIntent {
    const val EXTRA_ID = "rem_id"
    const val EXTRA_TITLE = "rem_title"
    const val EXTRA_TEXT = "rem_text"
    const val EXTRA_AMOUNT = "rem_amount"
    const val EXTRA_CURRENCY = "rem_currency"
    const val EXTRA_REPEAT = "rem_repeat"
    const val EXTRA_DUE = "rem_due"
    const val EXTRA_DAYS = "rem_days"
    const val EXTRA_MINUTE = "rem_minute"

    /** Next moment to fire: dueDate − notifyDaysBefore at the reminder's time of day, advanced past now for repeats. */
    fun triggerAt(r: Reminder, zone: ZoneId = ZoneId.systemDefault()): Long? {
        val now = System.currentTimeMillis()
        val hour = (r.notifyMinuteOfDay / 60).coerceIn(0, 23)
        val minute = (r.notifyMinuteOfDay % 60).coerceIn(0, 59)
        var due = Instant.ofEpochMilli(r.dueDate).atZone(zone).toLocalDate()
        fun notifyMillis(d: LocalDate): Long =
            d.minusDays(r.notifyDaysBefore.toLong()).atTime(hour, minute)
                .atZone(zone).toInstant().toEpochMilli()

        var t = notifyMillis(due)
        if (t > now) return t
        if (r.repeat == RepeatType.NONE) return null
        var guard = 0
        while (t <= now && guard < 1200) {
            due = advance(due, r.repeat)
            t = notifyMillis(due)
            guard++
        }
        return if (t > now) t else null
    }

    private fun advance(d: LocalDate, repeat: RepeatType): LocalDate = when (repeat) {
        RepeatType.WEEKLY -> d.plusWeeks(1)
        RepeatType.MONTHLY -> d.plusMonths(1)
        RepeatType.YEARLY -> d.plusYears(1)
        RepeatType.NONE -> d
    }

    fun buildIntent(context: Context, r: Reminder): Intent =
        Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_ID, r.id)
            putExtra(EXTRA_TITLE, r.title)
            putExtra(EXTRA_TEXT, r.amount?.let { CurrencyFormatter.format(it, r.currency ?: Currency.RUB) })
            r.amount?.let { putExtra(EXTRA_AMOUNT, it) }
            putExtra(EXTRA_CURRENCY, r.currency?.name)
            putExtra(EXTRA_REPEAT, r.repeat.name)
            putExtra(EXTRA_DUE, r.dueDate)
            putExtra(EXTRA_DAYS, r.notifyDaysBefore)
            putExtra(EXTRA_MINUTE, r.notifyMinuteOfDay)
        }
}

internal fun scheduleReminderAlarm(context: Context, r: Reminder) {
    val am = context.getSystemService(AlarmManager::class.java) ?: return
    val triggerAt = ReminderIntent.triggerAt(r) ?: run { cancelReminderAlarm(context, r.id); return }
    val pi = PendingIntent.getBroadcast(
        context,
        r.id.toInt(),
        ReminderIntent.buildIntent(context, r),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    // Inexact alarm — reminders don't need exact timing, so no SCHEDULE_EXACT_ALARM permission.
    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
}

internal fun cancelReminderAlarm(context: Context, id: Long) {
    val am = context.getSystemService(AlarmManager::class.java) ?: return
    val pi = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        Intent(context, ReminderAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    am.cancel(pi)
    pi.cancel()
}

/** Injectable facade so view models can (re)schedule without touching AlarmManager directly. */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(reminder: Reminder) = scheduleReminderAlarm(context, reminder)
    fun cancel(id: Long) = cancelReminderAlarm(context, id)
    fun scheduleAll(reminders: List<Reminder>) = reminders.forEach { scheduleReminderAlarm(context, it) }
}
