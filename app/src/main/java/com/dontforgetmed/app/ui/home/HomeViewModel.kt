package com.dontforgetmed.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dontforgetmed.app.data.MedicationRepository
import com.dontforgetmed.app.data.entity.DoseLog
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.data.entity.Medication
import com.dontforgetmed.app.data.entity.Schedule
import com.dontforgetmed.app.util.Time
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayDose(
    val log: DoseLog,
    val medication: Medication,
    val schedule: Schedule,
    val scheduledAt: Long,
)

class HomeViewModel(private val repo: MedicationRepository) : ViewModel() {

    private val from = Time.startOfDay()
    private val to = Time.endOfDay()
    private val todayBit = Time.dayOfWeekBit()

    val doses: StateFlow<List<TodayDose>> = combine(
        repo.medications,
        repo.activeSchedules,
        repo.dosesBetween(from, to),
    ) { meds, schedules, logs ->
        val medById = meds.associateBy { it.id }
        val scheduleById = schedules.associateBy { it.id }

        // ensure a log exists for every today-applicable schedule
        schedules
            .filter { (it.daysOfWeek and todayBit) != 0 && medById.containsKey(it.medicationId) }
            .forEach { sch ->
                val at = Time.scheduledAtToday(sch.minuteOfDay)
                viewModelScope.launch {
                    repo.ensureDoseLog(sch.medicationId, sch.id, at)
                }
            }

        logs.mapNotNull { log ->
            val med = medById[log.medicationId] ?: return@mapNotNull null
            val sch = scheduleById[log.scheduleId] ?: return@mapNotNull null
            TodayDose(log, med, sch, log.scheduledAt)
        }.sortedBy { it.scheduledAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markTaken(log: DoseLog) = viewModelScope.launch { repo.markDose(log.id, DoseStatus.TAKEN) }
    fun markSkipped(log: DoseLog) = viewModelScope.launch { repo.markDose(log.id, DoseStatus.SKIPPED) }

    class Factory(private val repo: MedicationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repo) as T
    }
}
