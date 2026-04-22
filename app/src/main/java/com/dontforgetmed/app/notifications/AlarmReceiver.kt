package com.dontforgetmed.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dontforgetmed.app.DontForgetMedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1L)
        val medicationId = intent.getLongExtra(AlarmScheduler.EXTRA_MEDICATION_ID, -1L)
        val scheduledAt = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_AT, 0L)
        if (scheduleId <= 0 || medicationId <= 0) return

        val pending = goAsync()
        val app = context.applicationContext as DontForgetMedApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = app.repository
                val medication = repo.getMedication(medicationId) ?: return@launch
                val logId = repo.ensureDoseLog(medicationId, scheduleId, scheduledAt)
                ReminderNotifications.show(context, medication, scheduleId, logId, scheduledAt)

                // Schedule next occurrence (setExact doesn't repeat)
                val schedules = repo.getSchedulesFor(medicationId)
                schedules.firstOrNull { it.id == scheduleId }?.let {
                    AlarmScheduler.scheduleNext(context, it)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
