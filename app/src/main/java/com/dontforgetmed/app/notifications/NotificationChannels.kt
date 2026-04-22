package com.dontforgetmed.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val REMINDERS_ID = "reminders"
    const val STOCK_ID = "stock"

    fun ensure(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(REMINDERS_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(REMINDERS_ID, "תזכורות לתרופות", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "התראות לנטילת תרופות בזמן"
                    enableVibration(true)
                }
            )
        }
        if (nm.getNotificationChannel(STOCK_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(STOCK_ID, "מלאי נמוך", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "התראות על מלאי תרופות שעומד להיגמר"
                }
            )
        }
    }
}
