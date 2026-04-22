package com.dontforgetmed.app

import android.app.Application
import com.dontforgetmed.app.data.AppDatabase
import com.dontforgetmed.app.data.MedicationRepository

class DontForgetMedApp : Application() {
    lateinit var repository: MedicationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = MedicationRepository(db.medicationDao(), db.scheduleDao(), db.doseLogDao())
    }
}
