package com.dontforgetmed.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dontforgetmed.app.DontForgetMedApp
import com.dontforgetmed.app.data.entity.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoseActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        if (logId <= 0) return

        when (intent.action) {
            ACTION_TAKE -> markStatus(context, logId, DoseStatus.TAKEN)
            ACTION_SKIP -> markStatus(context, logId, DoseStatus.SKIPPED)
            ACTION_SNOOZE -> {
                val minutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 15).coerceAtLeast(1)
                AlarmScheduler.scheduleSnooze(context, logId, minutes)
                ReminderNotifications.cancel(context, logId)
            }
        }
    }

    private fun markStatus(context: Context, logId: Long, status: DoseStatus) {
        val pending = goAsync()
        val app = context.applicationContext as DontForgetMedApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val med = app.repository.markDose(logId, status)
                ReminderNotifications.cancel(context, logId)
                if (status == DoseStatus.TAKEN && med != null) {
                    StockNotifications.maybeNotifyLowStock(context, med)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TAKE = "com.dontforgetmed.app.action.TAKE"
        const val ACTION_SKIP = "com.dontforgetmed.app.action.SKIP"
        const val ACTION_SNOOZE = "com.dontforgetmed.app.action.SNOOZE"
        const val EXTRA_LOG_ID = "logId"
        const val EXTRA_SNOOZE_MINUTES = "snoozeMinutes"
    }
}
