package com.example.stepeeeasy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stepeeeasy.data.local.entity.GpsPointEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for GPS Point entities.
 *
 * GPS points represent the path taken during a walk.
 * They are recorded periodically (every ~5 seconds) during an active walk.
 */
@Dao
interface GpsPointDao {

    // ========================================
    // CREATE operations
    // ========================================

    /**
     * Insert a single GPS point.
     *
     * Used when recording a new location during an active walk.
     *
     * Explanation:
     * - @Insert generates an INSERT SQL statement
     * - OnConflictStrategy.REPLACE means if there's a duplicate ID, replace it
     * - suspend makes this non-blocking (runs on background thread)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsPoint(gpsPoint: GpsPointEntity)

    /**
     * Insert multiple GPS points at once.
     *
     * More efficient than inserting one at a time.
     * Used for batch inserts (e.g., inserting 10 points collected over 50 seconds).
     *
     * Explanation:
     * - List<GpsPointEntity> means you can pass multiple points
     * - Room will insert all of them in a single database transaction
     * - This is faster and more efficient than calling insertGpsPoint() multiple times
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsPoints(gpsPoints: List<GpsPointEntity>)

    // ========================================
    // READ operations
    // ========================================

    /**
     * Get all GPS points for a specific walk, ordered by timestamp.
     *
     * Used to draw the path on the map in the Paths screen.
     *
     * @param walkId The ID of the walk
     * @return Flow of GPS points (emits updates if points are added during active walk)
     *
     * Explanation:
     * - WHERE walk_id = :walkId filters to only points for this specific walk
     * - ORDER BY timestamp ASC sorts points from oldest to newest (start → end)
     * - Flow means the UI will automatically update as new GPS points are added
     */
    @Query("SELECT * FROM gps_points WHERE walk_id = :walkId ORDER BY timestamp ASC")
    fun getGpsPointsForWalk(walkId: Long): Flow<List<GpsPointEntity>>

    /**
     * Get GPS points for a walk as a one-time query (not reactive).
     *
     * Used when you just need the points once (e.g., for export or calculation).
     *
     * Explanation:
     * - suspend instead of Flow means this fetches data once and returns
     * - Useful when you don't need live updates
     */
    @Query("SELECT * FROM gps_points WHERE walk_id = :walkId ORDER BY timestamp ASC")
    suspend fun getGpsPointsForWalkOnce(walkId: Long): List<GpsPointEntity>

    /**
     * Count how many GPS points a walk has.
     *
     * Useful for checking if a walk has enough GPS data to show a path.
     *
     * Explanation:
     * - COUNT(*) counts the number of rows
     * - Returns 0 if no GPS points exist for this walk
     */
    @Query("SELECT COUNT(*) FROM gps_points WHERE walk_id = :walkId")
    suspend fun getGpsPointCountForWalk(walkId: Long): Int

    // ========================================
    // DELETE operations
    // ========================================

    /**
     * Delete all GPS points for a specific walk.
     *
     * Note: This is usually not needed because GPS points are automatically
     * deleted when a walk is deleted (due to CASCADE in GpsPointEntity).
     *
     * Explanation:
     * - WHERE walk_id = :walkId deletes only points for this specific walk
     */
    @Query("DELETE FROM gps_points WHERE walk_id = :walkId")
    suspend fun deleteGpsPointsForWalk(walkId: Long)

    /**
     * Delete all GPS points from the database.
     *
     * This is also not usually needed (CASCADE handles it), but provided for completeness.
     */
    @Query("DELETE FROM gps_points")
    suspend fun deleteAllGpsPoints()
}