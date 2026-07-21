package com.farmmanager.app.data.dao

import androidx.room.*
import com.farmmanager.app.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun getAll(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE flockId = :flockId ORDER BY date DESC")
    fun getByFlock(flockId: Long): Flow<List<HealthRecord>>

    @Insert
    suspend fun insert(record: HealthRecord): Long

    @Update
    suspend fun update(record: HealthRecord)

    @Delete
    suspend fun delete(record: HealthRecord)

    @Query("DELETE FROM health_records")
    suspend fun clearAll()
}
