package com.financeapp.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.model.RepeatType

/** Fired by AlarmManager: posts the notification and, for repeating reminders, arms the next one. */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderIntent.EXTRA_ID, 0L)
        val title = intent.getStringExtra(ReminderIntent.EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(ReminderIntent.EXTRA_TEXT)
        ReminderNotifications.show(context, id, title, text)

        val repeat = runCatching {
            RepeatType.valueOf(intent.getStringExtra(ReminderIntent.EXTRA_REPEAT) ?: RepeatType.NONE.name)
        }.getOrDefault(RepeatType.NONE)

        if (repeat != RepeatType.NONE) {
            val amount = if (intent.hasExtra(ReminderIntent.EXTRA_AMOUNT)) {
                intent.getDoubleExtra(ReminderIntent.EXTRA_AMOUNT, Double.NaN).takeIf { !it.isNaN() }
            } else {
                null
            }
            val currency = intent.getStringExtra(ReminderIntent.EXTRA_CURRENCY)
                ?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
            val next = Reminder(
                id = id,
                title = title,
                amount = amount,
                currency = currency,
                dueDate = intent.getLongExtra(ReminderIntent.EXTRA_DUE, 0L),
                notifyDaysBefore = intent.getIntExtra(ReminderIntent.EXTRA_DAYS, 0),
                repeat = repeat,
            )
            scheduleReminderAlarm(context, next)
        }
    }
}
