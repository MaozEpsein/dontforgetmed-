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
        val pending = goAsync()
        val app = context.applicationContext as DontForgetMedApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    AlarmScheduler.ACTION_SNOOZE_FIRE -> handleSnoozeFire(context, app, intent)
                    else -> handleRegularFire(context, app, intent)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleRegularFire(context: Context, app: DontForgetMedApp, intent: Intent) {
        val scheduleId = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1L)
        val medicationId = intent.getLongExtra(AlarmScheduler.EXTRA_MEDICATION_ID, -1L)
        val scheduledAt = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_AT, 0L)
        if (scheduleId <= 0 || medicationId <= 0) return

        val repo = app.repository
        val medication = repo.getMedication(medicationId) ?: return
        val logId = repo.ensureDoseLog(medicationId, scheduleId, scheduledAt)
        ReminderNotifications.show(context, medication, scheduleId, logId, scheduledAt)

        // schedule next recurring occurrence
        val schedules = repo.getSchedulesFor(medicationId)
        schedules.firstOrNull { it.id == scheduleId }?.let {
            AlarmScheduler.scheduleNext(context, it)
        }
    }

    private suspend fun handleSnoozeFire(context: Context, app: DontForgetMedApp, intent: Intent) {
        val logId = intent.getLongExtra(AlarmScheduler.EXTRA_LOG_ID, -1L)
        if (logId <= 0) return
        val repo = app.repository
        val log = repo.getDoseLog(logId) ?: return
        val medication = repo.getMedication(log.medicationId) ?: return
        ReminderNotifications.show(context, medication, log.scheduleId, logId, log.scheduledAt)
    }
}
