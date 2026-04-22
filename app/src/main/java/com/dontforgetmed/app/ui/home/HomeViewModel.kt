package com.dontforgetmed.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dontforgetmed.app.data.MedicationRepository
import com.dontforgetmed.app.data.entity.DoseLog
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.data.entity.FrequencyType
import com.dontforgetmed.app.data.entity.Medication
import com.dontforgetmed.app.data.entity.Schedule
import com.dontforgetmed.app.notifications.StockNotifications
import com.dontforgetmed.app.util.Time
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayDose(
    val log: DoseLog,
    val medication: Medication,
    val schedule: Schedule,
    val scheduledAt: Long,
)

sealed interface HomeEvent {
    data class DoseMarked(val logId: Long, val takenLabel: Boolean) : HomeEvent
}

class HomeViewModel(
    private val repo: MedicationRepository,
    private val appContext: Context,
) : ViewModel() {

    private val from = Time.startOfDay()
    private val to = Time.endOfDay()
    private val todayBit = Time.dayOfWeekBit()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val doses: StateFlow<List<TodayDose>> = combine(
        repo.medications,
        repo.activeSchedules,
        repo.dosesBetween(from, to),
    ) { meds, schedules, logs ->
        val medById = meds.associateBy { it.id }
        val scheduleById = schedules.associateBy { it.id }

        schedules
            .filter { medById.containsKey(it.medicationId) }
            .forEach { sch ->
                val times = occurrencesToday(sch)
                times.forEach { at ->
                    viewModelScope.launch { repo.ensureDoseLog(sch.medicationId, sch.id, at) }
                }
            }

        logs.mapNotNull { log ->
            val med = medById[log.medicationId] ?: return@mapNotNull null
            val sch = scheduleById[log.scheduleId] ?: return@mapNotNull null
            TodayDose(log, med, sch, log.scheduledAt)
        }.sortedBy { it.scheduledAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markTaken(log: DoseLog) = viewModelScope.launch {
        val med = repo.markDose(log.id, DoseStatus.TAKEN)
        med?.let { StockNotifications.maybeNotifyLowStock(appContext, it) }
        _events.trySend(HomeEvent.DoseMarked(log.id, takenLabel = true))
    }

    fun markSkipped(log: DoseLog) = viewModelScope.launch {
        repo.markDose(log.id, DoseStatus.SKIPPED)
        _events.trySend(HomeEvent.DoseMarked(log.id, takenLabel = false))
    }

    fun undo(logId: Long) = viewModelScope.launch {
        repo.revertDose(logId)
    }

    private fun occurrencesToday(sch: Schedule): List<Long> = when (sch.frequencyType) {
        FrequencyType.DAILY_AT_TIME -> {
            if ((sch.daysOfWeek and todayBit) != 0) listOf(Time.scheduledAtToday(sch.minuteOfDay)) else emptyList()
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
            if (daysFromStart % n == 0L) listOf(Time.scheduledAtToday(sch.minuteOfDay)) else emptyList()
        }
    }

    class Factory(
        private val repo: MedicationRepository,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repo, appContext) as T
    }
}
