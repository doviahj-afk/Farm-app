package com.farmmanager.app.data.dao

import androidx.room.*
import com.farmmanager.app.data.entity.FeedRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_records ORDER BY date DESC")
    fun getAll(): Flow<List<FeedRecord>>

    @Query("SELECT * FROM feed_records WHERE flockId = :flockId ORDER BY date DESC")
    fun getByFlock(flockId: Long): Flow<List<FeedRecord>>

    @Insert
    suspend fun insert(record: FeedRecord): Long

    @Update
    suspend fun update(record: FeedRecord)

    @Delete
    suspend fun delete(record: FeedRecord)

    @Query("DELETE FROM feed_records")
    suspend fun clearAll()

    @Query("SELECT COALESCE(SUM(cost), 0) FROM feed_records WHERE date BETWEEN :start AND :end")
    fun getTotalCostBetween(start: Long, end: Long): Flow<Double>
}
