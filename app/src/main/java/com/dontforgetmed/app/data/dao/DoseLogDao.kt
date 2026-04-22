package com.dontforgetmed.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dontforgetmed.app.data.entity.DoseLog
import com.dontforgetmed.app.data.entity.DoseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE scheduledAt BETWEEN :from AND :to ORDER BY scheduledAt")
    fun observeInRange(from: Long, to: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getById(id: Long): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE scheduleId = :scheduleId AND scheduledAt = :scheduledAt LIMIT 1")
    suspend fun find(scheduleId: Long, scheduledAt: Long): DoseLog?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: DoseLog): Long

    @Update
    suspend fun update(log: DoseLog)

    @Query("UPDATE dose_logs SET status = :status, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun setStatus(id: Long, status: DoseStatus, resolvedAt: Long)
}
