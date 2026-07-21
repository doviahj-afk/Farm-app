package com.farmmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "egg_records")
data class EggRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flockId: Long,
    val date: Long,
    val quantityCollected: Int,
    val quantityBroken: Int = 0,
    val notes: String = "",
    /** Cage number 1-80 that this record belongs to. 0 = whole-flock entry (no specific cage). */
    val cageNumber: Int = 0
)
