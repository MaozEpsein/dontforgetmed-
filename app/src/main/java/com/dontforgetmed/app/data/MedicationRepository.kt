package com.dontforgetmed.app.data

import com.dontforgetmed.app.data.dao.DoseLogDao
import com.dontforgetmed.app.data.dao.MedicationDao
import com.dontforgetmed.app.data.dao.ScheduleDao
import com.dontforgetmed.app.data.entity.DoseLog
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.data.entity.Medication
import com.dontforgetmed.app.data.entity.Schedule
import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val scheduleDao: ScheduleDao,
    private val doseLogDao: DoseLogDao,
) {
    val medications: Flow<List<Medication>> = medicationDao.observeAll()
    val activeSchedules: Flow<List<Schedule>> = scheduleDao.observeActive()

    fun dosesBetween(from: Long, to: Long): Flow<List<DoseLog>> =
        doseLogDao.observeInRange(from, to)

    suspend fun getMedication(id: Long) = medicationDao.getById(id)
    suspend fun getSchedulesFor(medicationId: Long) = scheduleDao.getForMedication(medicationId)

    suspend fun upsertMedication(m: Medication): Long =
        if (m.id == 0L) medicationDao.insert(m) else { medicationDao.update(m); m.id }

    suspend fun deleteMedication(m: Medication) = medicationDao.delete(m)

    suspend fun replaceSchedules(medicationId: Long, schedules: List<Schedule>) {
        scheduleDao.deleteForMedication(medicationId)
        schedules.forEach { scheduleDao.insert(it.copy(medicationId = medicationId)) }
    }

    suspend fun markDose(logId: Long, status: DoseStatus) {
        doseLogDao.setStatus(logId, status, System.currentTimeMillis())
        if (status == DoseStatus.TAKEN) {
            doseLogDao.getById(logId)?.let { medicationDao.decrementStock(it.medicationId) }
        }
    }

    suspend fun ensureDoseLog(medicationId: Long, scheduleId: Long, scheduledAt: Long): Long {
        doseLogDao.find(scheduleId, scheduledAt)?.let { return it.id }
        return doseLogDao.insert(
            DoseLog(medicationId = medicationId, scheduleId = scheduleId, scheduledAt = scheduledAt)
        )
    }
}
