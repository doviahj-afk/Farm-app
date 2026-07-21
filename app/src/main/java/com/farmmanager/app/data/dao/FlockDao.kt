package com.farmmanager.app.data.dao

import androidx.room.*
import com.farmmanager.app.data.entity.Flock
import kotlinx.coroutines.flow.Flow

@Dao
interface FlockDao {
    @Query("SELECT * FROM flocks ORDER BY acquisitionDate DESC")
    fun getAll(): Flow<List<Flock>>

    @Query("SELECT * FROM flocks WHERE id = :id")
    suspend fun getById(id: Long): Flock?

    @Insert
    suspend fun insert(flock: Flock): Long

    @Update
    suspend fun update(flock: Flock)

    @Delete
    suspend fun delete(flock: Flock)

    @Query("DELETE FROM flocks")
    suspend fun clearAll()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM flocks")
    fun getTotalBirds(): Flow<Int>
}
