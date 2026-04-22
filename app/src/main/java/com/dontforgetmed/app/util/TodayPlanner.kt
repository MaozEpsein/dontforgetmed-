package com.dontforgetmed.app.util

import com.dontforgetmed.app.data.MedicationRepository
import com.dontforgetmed.app.data.entity.FrequencyType
import com.dontforgetmed.app.data.entity.Schedule
import kotlinx.coroutines.flow.first

object TodayPlanner {

    suspend fun ensureTodayLogs(repo: MedicationRepository) {
        val todayBit = Time.dayOfWeekBit()
        val medIds = repo.medications.first().map { it.id }.toSet()
        val schedules = repo.activeSchedules.first()
        schedules.filter { it.medicationId in medIds }.forEach { sch ->
            occurrencesToday(sch, todayBit).forEach { at ->
                repo.ensureDoseLog(sch.medicationId, sch.id, at)
            }
        }
    }

    fun occurrencesToday(sch: Schedule, todayBit: Int = Time.dayOfWeekBit()): List<Long> =
        when (sch.frequencyType) {
            FrequencyType.DAILY_AT_TIME -> {
                if ((sch.daysOfWeek and todayBit) != 0) listOf(Time.scheduledAtToday(sch.minuteOfDay))
                else emptyList()
            }
            FrequencyType.EVERY_N_HOURS -> {
                val hours = sch.intervalHours.coerceAtLeast(1)
                val step = hours * 60
                generateSequence(sch.minuteOfDay) { it + step }
                    .takeWhile { it < 24 * 60 }
                    .map { Time.scheduledAtToday(it) }
                    .toList()
            }
            FrequencyType.EVERY_N_DAYS -> {
                val n = sch.intervalDays.coerceAtLeast(1)
                val start = if (sch.startDate > 0) Time.startOfDay(sch.startDate) else Time.startOfDay()
                val today = Time.startOfDay()
                val daysFromStart = ((today - start) / (24L * 60 * 60 * 1000)).coerceAtLeast(0)
                if (daysFromStart % n == 0L) listOf(Time.scheduledAtToday(sch.minuteOfDay))
                else emptyList()
            }
        }
}
