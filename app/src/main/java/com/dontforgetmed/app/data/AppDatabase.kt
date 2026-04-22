package com.dontforgetmed.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dontforgetmed.app.data.dao.DoseLogDao
import com.dontforgetmed.app.data.dao.MedicationDao
import com.dontforgetmed.app.data.dao.ScheduleDao
import com.dontforgetmed.app.data.entity.DoseLog
import com.dontforgetmed.app.data.entity.Medication
import com.dontforgetmed.app.data.entity.Schedule

@Database(
    entities = [Medication::class, Schedule::class, DoseLog::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dontforgetmed.db",
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
