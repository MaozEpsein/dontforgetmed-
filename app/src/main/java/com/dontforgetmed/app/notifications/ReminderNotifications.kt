package com.dontforgetmed.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dontforgetmed.app.MainActivity
import com.dontforgetmed.app.R
import com.dontforgetmed.app.data.entity.Medication

object ReminderNotifications {

    fun notificationId(logId: Long): Int = (1_000_000 + logId).toInt()

    fun show(
        context: Context,
        medication: Medication,
        scheduleId: Long,
        logId: Long,
        scheduledAt: Long,
    ) {
        NotificationChannels.ensure(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context, logId.toInt(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val takePi = actionPendingIntent(context, logId, DoseActionReceiver.ACTION_TAKE)
        val skipPi = actionPendingIntent(context, logId, DoseActionReceiver.ACTION_SKIP)

        val dosageLine = medication.dosage.ifBlank { context.getString(R.string.notif_default_body) }
        val title = context.getString(R.string.notif_title, medication.name)

        val builder = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(dosageLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dosageLine))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .addAction(0, context.getString(R.string.take), takePi)
            .addAction(0, context.getString(R.string.skip), skipPi)
            .setWhen(scheduledAt)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId(logId), builder.build())
    }

    fun cancel(context: Context, logId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId(logId))
    }

    private fun actionPendingIntent(context: Context, logId: Long, action: String): PendingIntent {
        val intent = Intent(context, DoseActionReceiver::class.java).apply {
            this.action = action
            putExtra(DoseActionReceiver.EXTRA_LOG_ID, logId)
        }
        val requestCode = (logId.toInt() shl 4) or (if (action == DoseActionReceiver.ACTION_TAKE) 1 else 2)
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
