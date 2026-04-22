package com.dontforgetmed.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val notes: String = "",
    val stockCount: Int = 0,
    val lowStockThreshold: Int = 5,
    val colorHex: String = "#00897B",
    val iconKey: String = "pill",
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
)
