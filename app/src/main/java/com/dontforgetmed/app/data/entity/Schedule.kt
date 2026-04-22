package com.dontforgetmed.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FrequencyType { DAILY_AT_TIME, EVERY_N_HOURS, EVERY_N_DAYS }

// DAILY_AT_TIME: daysOfWeek bitmask (bit 0 = Sunday..6 = Saturday), minuteOfDay is the trigger.
// EVERY_N_HOURS: anchor at minuteOfDay, repeat every intervalHours (while within a day window).
// EVERY_N_DAYS: minuteOfDay on every Nth day from a start date (intervalDays).
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("medicationId")],
)
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val minuteOfDay: Int,
    val daysOfWeek: Int = 127,
    val active: Boolean = true,
    val frequencyType: FrequencyType = FrequencyType.DAILY_AT_TIME,
    val intervalHours: Int = 0,
    val intervalDays: Int = 1,
    val startDate: Long = 0L,
)
