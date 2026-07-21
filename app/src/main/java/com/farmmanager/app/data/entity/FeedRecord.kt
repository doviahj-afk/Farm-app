package com.farmmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_records")
data class FeedRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flockId: Long,
    val feedName: String,
    val date: Long,
    val quantityKg: Double,
    val cost: Double,
    val notes: String = ""
)
