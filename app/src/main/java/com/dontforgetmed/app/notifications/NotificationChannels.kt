package com.dontforgetmed.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val REMINDERS_ID = "reminders"

    fun ensure(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(REMINDERS_ID) == null) {
            val channel = NotificationChannel(
                REMINDERS_ID,
                "תזכורות לתרופות",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "התראות לנטילת תרופות בזמן"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }
}
