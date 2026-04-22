package com.dontforgetmed.app.ui.edit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dontforgetmed.app.data.MedicationRepository
import com.dontforgetmed.app.data.entity.Medication
import com.dontforgetmed.app.data.entity.Schedule
import com.dontforgetmed.app.notifications.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimeEntry(val minuteOfDay: Int, val daysOfWeek: Int = 127)

data class EditUiState(
    val id: Long = 0,
    val name: String = "",
    val dosage: String = "",
    val notes: String = "",
    val stockCount: String = "0",
    val lowStockThreshold: String = "5",
    val colorHex: String = "#00897B",
    val times: List<TimeEntry> = listOf(TimeEntry(8 * 60)),
    val loading: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && times.isNotEmpty()
}

class EditMedicationViewModel(
    private val repo: MedicationRepository,
    private val appContext: Context,
    private val medicationId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState(loading = medicationId > 0))
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    init { if (medicationId > 0) load() }

    private fun load() = viewModelScope.launch {
        val med = repo.getMedication(medicationId) ?: return@launch
        val schedules = repo.getSchedulesFor(medicationId)
        _state.value = EditUiState(
            id = med.id,
            name = med.name,
            dosage = med.dosage,
            notes = med.notes,
            stockCount = med.stockCount.toString(),
            lowStockThreshold = med.lowStockThreshold.toString(),
            colorHex = med.colorHex,
            times = schedules.map { TimeEntry(it.minuteOfDay, it.daysOfWeek) }
                .ifEmpty { listOf(TimeEntry(8 * 60)) },
            loading = false,
        )
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDosage(v: String) = _state.update { it.copy(dosage = v) }
    fun setNotes(v: String) = _state.update { it.copy(notes = v) }
    fun setStock(v: String) = _state.update { it.copy(stockCount = v.filter(Char::isDigit)) }
    fun setLowStock(v: String) = _state.update { it.copy(lowStockThreshold = v.filter(Char::isDigit)) }
    fun setColor(hex: String) = _state.update { it.copy(colorHex = hex) }

    fun addTime(minuteOfDay: Int) = _state.update {
        it.copy(times = (it.times + TimeEntry(minuteOfDay)).sortedBy { t -> t.minuteOfDay })
    }

    fun updateTime(index: Int, minuteOfDay: Int) = _state.update {
        it.copy(times = it.times.toMutableList().also { list ->
            list[index] = list[index].copy(minuteOfDay = minuteOfDay)
        }.sortedBy { t -> t.minuteOfDay })
    }

    fun removeTime(index: Int) = _state.update {
        it.copy(times = it.times.toMutableList().also { list -> list.removeAt(index) })
    }

    fun toggleDay(index: Int, dayBit: Int) = _state.update {
        it.copy(times = it.times.toMutableList().also { list ->
            val e = list[index]
            list[index] = e.copy(daysOfWeek = e.daysOfWeek xor dayBit)
        })
    }

    fun save() = viewModelScope.launch {
        val s = _state.value
        if (!s.canSave) return@launch
        val med = Medication(
            id = s.id,
            name = s.name.trim(),
            dosage = s.dosage.trim(),
            notes = s.notes.trim(),
            stockCount = s.stockCount.toIntOrNull() ?: 0,
            lowStockThreshold = s.lowStockThreshold.toIntOrNull() ?: 5,
            colorHex = s.colorHex,
        )
        val savedId = repo.upsertMedication(med)
        // cancel previous alarms for this medication
        repo.getSchedulesFor(savedId).forEach { AlarmScheduler.cancel(appContext, it) }
        repo.replaceSchedules(
            savedId,
            s.times.map { Schedule(medicationId = savedId, minuteOfDay = it.minuteOfDay, daysOfWeek = it.daysOfWeek) }
        )
        // reschedule with new ids
        val newSchedules = repo.getSchedulesFor(savedId)
        newSchedules.forEach { AlarmScheduler.scheduleNext(appContext, it) }
        _state.update { it.copy(saved = true) }
    }

    fun delete() = viewModelScope.launch {
        val s = _state.value
        if (s.id > 0) {
            repo.getSchedulesFor(s.id).forEach { AlarmScheduler.cancel(appContext, it) }
            repo.getMedication(s.id)?.let { repo.deleteMedication(it) }
        }
        _state.update { it.copy(saved = true) }
    }

    class Factory(
        private val repo: MedicationRepository,
        private val appContext: Context,
        private val medicationId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditMedicationViewModel(repo, appContext, medicationId) as T
    }
}
