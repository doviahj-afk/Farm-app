package com.farmmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flockId: Long,
    val date: Long,
    val type: String,
    val description: String,
    val cost: Double = 0.0,
    val notes: String = ""
)
