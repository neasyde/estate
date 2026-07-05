package com.financeapp.core.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.financeapp.R

object ReminderNotifications {
    const val CHANNEL_ID = "reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java) ?: return
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.rem_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.rem_channel_desc) }
                mgr.createNotificationChannel(channel)
            }
        }
    }

    fun show(context: Context, id: Long, title: String, text: String?) {
        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = launch?.let {
            PendingIntent.getActivity(
                context,
                id.toInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .apply { if (!text.isNullOrBlank()) setContentText(text) }
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Guard against a re-alert if the same reminder id is posted twice (e.g. the alarm is
            // delivered more than once): the notification updates silently instead of buzzing again.
            .setOnlyAlertOnce(true)
            .build()

        try {
            manager.notify(id.toInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS revoked between the check and the call — ignore.
        }
    }
}
