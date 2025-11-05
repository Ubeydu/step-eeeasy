package com.example.stepeeeasy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stepeeeasy.data.local.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Walk entities.
 */
@Dao
interface WalkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalk(walk: WalkEntity): Long

    @Query("SELECT * FROM walks WHERE is_active = 1 LIMIT 1")
    fun getActiveWalk(): Flow<WalkEntity?>

    @Query("SELECT * FROM walks WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveWalkNow(): WalkEntity?

    @Query("SELECT * FROM walks ORDER BY start_time DESC")
    fun getAllWalks(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE date BETWEEN :startDate AND :endDate ORDER BY start_time DESC")
    fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE id = :walkId")
    suspend fun getWalkById(walkId: Long): WalkEntity?

    @Update
    suspend fun updateWalk(walk: WalkEntity)

    @Query("DELETE FROM walks")
    suspend fun deleteAllWalks()

    @Query("DELETE FROM walks WHERE id = :walkId")
    suspend fun deleteWalkById(walkId: Long)
}