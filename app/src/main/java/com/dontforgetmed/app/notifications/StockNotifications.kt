package com.dontforgetmed.app.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.dontforgetmed.app.R
import com.dontforgetmed.app.data.entity.Medication

object StockNotifications {

    private fun notificationId(medId: Long): Int = (2_000_000 + medId).toInt()

    fun maybeNotifyLowStock(context: Context, medication: Medication) {
        if (medication.stockCount > medication.lowStockThreshold) return
        if (medication.lowStockThreshold <= 0) return

        NotificationChannels.ensure(context)
        val title = context.getString(R.string.stock_low_title, medication.name)
        val body = if (medication.stockCount <= 0)
            context.getString(R.string.stock_empty_body)
        else
            context.getString(R.string.stock_low_body, medication.stockCount)

        val builder = NotificationCompat.Builder(context, NotificationChannels.STOCK_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId(medication.id), builder.build())
    }
}
