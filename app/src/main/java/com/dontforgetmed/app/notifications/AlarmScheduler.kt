package com.dontforgetmed.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dontforgetmed.app.data.entity.Schedule
import com.dontforgetmed.app.util.Time
import java.util.Calendar

object AlarmScheduler {

    const val ACTION_FIRE = "com.dontforgetmed.app.action.FIRE"
    const val EXTRA_SCHEDULE_ID = "scheduleId"
    const val EXTRA_MEDICATION_ID = "medicationId"
    const val EXTRA_SCHEDULED_AT = "scheduledAt"

    fun scheduleNext(context: Context, schedule: Schedule) {
        if (!schedule.active || schedule.daysOfWeek == 0) return
        val nextAt = computeNextTrigger(schedule) ?: return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pi = buildPendingIntent(context, schedule, nextAt)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAt, pi)
        }
    }

    fun cancel(context: Context, schedule: Schedule) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, schedule, 0L, flagsOverride = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            ?: return
        am.cancel(pi)
        pi.cancel()
    }

    fun rescheduleAll(context: Context, schedules: List<Schedule>) {
        schedules.forEach { scheduleNext(context, it) }
    }

    private fun buildPendingIntent(
        context: Context,
        schedule: Schedule,
        scheduledAt: Long,
        flagsOverride: Int? = null,
    ): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_MEDICATION_ID, schedule.medicationId)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
        }
        val flags = flagsOverride ?: (PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return PendingIntent.getBroadcast(context, schedule.id.toInt(), intent, flags)
    }

    private fun computeNextTrigger(schedule: Schedule): Long? {
        val now = System.currentTimeMillis()
        for (offset in 0..7) {
            val dayStart = Time.startOfDay(now + offset * 24L * 60 * 60 * 1000)
            val c = Calendar.getInstance().apply { timeInMillis = dayStart }
            val bit = 1 shl (c.get(Calendar.DAY_OF_WEEK) - 1)
            if ((schedule.daysOfWeek and bit) == 0) continue
            val trigger = dayStart + schedule.minuteOfDay * 60_000L
            if (trigger > now) return trigger
        }
        return null
    }
}
