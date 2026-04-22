package com.dontforgetmed.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dontforgetmed.app.DontForgetMedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.MY_PACKAGE_REPLACED") return

        val pending = goAsync()
        val app = context.applicationContext as DontForgetMedApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedules = app.repository.activeSchedules.first()
                AlarmScheduler.rescheduleAll(context, schedules)
            } finally {
                pending.finish()
            }
        }
    }
}
