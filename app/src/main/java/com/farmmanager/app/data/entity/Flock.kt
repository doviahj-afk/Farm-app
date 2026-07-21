package com.farmmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flocks")
data class Flock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breed: String,
    val quantity: Int,
    val acquisitionDate: Long,
    val acquisitionCost: Double,
    val notes: String = "",
    /** When the birds were hatched/born. Used to compute age. Null if unknown. */
    val hatchDate: Long? = null,
    /** Number of cages allocated to this flock, 1-80. Used for per-cage egg logging. */
    val cageCount: Int = 1
)
