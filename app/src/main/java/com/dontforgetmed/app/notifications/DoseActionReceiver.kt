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
        val status = when (intent.action) {
            ACTION_TAKE -> DoseStatus.TAKEN
            ACTION_SKIP -> DoseStatus.SKIPPED
            else -> return
        }
        val pending = goAsync()
        val app = context.applicationContext as DontForgetMedApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.markDose(logId, status)
                ReminderNotifications.cancel(context, logId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TAKE = "com.dontforgetmed.app.action.TAKE"
        const val ACTION_SKIP = "com.dontforgetmed.app.action.SKIP"
        const val EXTRA_LOG_ID = "logId"
    }
}
