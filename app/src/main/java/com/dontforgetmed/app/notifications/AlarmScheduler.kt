package com.dontforgetmed.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dontforgetmed.app.data.entity.FrequencyType
import com.dontforgetmed.app.data.entity.Schedule
import com.dontforgetmed.app.util.Time
import java.util.Calendar

object AlarmScheduler {

    const val ACTION_FIRE = "com.dontforgetmed.app.action.FIRE"
    const val ACTION_SNOOZE_FIRE = "com.dontforgetmed.app.action.SNOOZE_FIRE"
    const val EXTRA_SCHEDULE_ID = "scheduleId"
    const val EXTRA_MEDICATION_ID = "medicationId"
    const val EXTRA_SCHEDULED_AT = "scheduledAt"
    const val EXTRA_LOG_ID = "logId"
    private const val SNOOZE_REQUEST_OFFSET = 3_000_000

    fun scheduleNext(context: Context, schedule: Schedule) {
        if (!schedule.active || schedule.daysOfWeek == 0) return
        val nextAt = computeNextTrigger(schedule) ?: return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pi = buildPendingIntent(context, schedule, nextAt) ?: return

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

    fun scheduleSnooze(context: Context, logId: Long, delayMinutes: Int) {
        val triggerAt = System.currentTimeMillis() + delayMinutes.coerceAtLeast(1) * 60_000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_FIRE
            putExtra(EXTRA_LOG_ID, logId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            (logId.toInt() + SNOOZE_REQUEST_OFFSET),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
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

    fun computeNextTrigger(schedule: Schedule, now: Long = System.currentTimeMillis()): Long? {
        return when (schedule.frequencyType) {
            FrequencyType.DAILY_AT_TIME -> nextDaily(schedule, now)
            FrequencyType.EVERY_N_HOURS -> nextEveryHours(schedule, now)
            FrequencyType.EVERY_N_DAYS -> nextEveryDays(schedule, now)
        }
    }

    private fun nextDaily(schedule: Schedule, now: Long): Long? {
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

    private fun nextEveryHours(schedule: Schedule, now: Long): Long? {
        val hours = schedule.intervalHours.coerceAtLeast(1)
        val step = hours * 60L * 60 * 1000
        // Anchor at today's minuteOfDay, then add steps until > now
        var next = Time.startOfDay(now) + schedule.minuteOfDay * 60_000L
        val limit = now + 48L * 60 * 60 * 1000
        while (next <= now && next < limit) next += step
        return if (next > now) next else null
    }

    private fun nextEveryDays(schedule: Schedule, now: Long): Long? {
        val n = schedule.intervalDays.coerceAtLeast(1)
        val start = if (schedule.startDate > 0) schedule.startDate else Time.startOfDay(now)
        val msPerDay = 24L * 60 * 60 * 1000
        val daysFromStart = ((Time.startOfDay(now) - Time.startOfDay(start)) / msPerDay).coerceAtLeast(0)
        val offset = ((n - (daysFromStart % n)) % n).toInt()
        for (o in offset..(offset + n * 2)) {
            val dayStart = Time.startOfDay(now + o * msPerDay)
            val trigger = dayStart + schedule.minuteOfDay * 60_000L
            if (trigger > now) return trigger
        }
        return null
    }
}
