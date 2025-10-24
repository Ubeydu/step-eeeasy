package com.example.stepeeeasy.data.repository

import com.example.stepeeeasy.data.local.dao.GpsPointDao
import com.example.stepeeeasy.data.local.dao.WalkDao
import com.example.stepeeeasy.data.local.entity.WalkEntity
import com.example.stepeeeasy.domain.model.GpsPoint
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementation of WalkRepository.
 *
 * This class is the TRANSLATOR between the database layer and the domain layer.
 * It converts WalkEntity (database format) to Walk (domain model) and vice versa.
 *
 * @Inject tells Hilt to automatically provide the dependencies (WalkDao, GpsPointDao)
 */
class WalkRepositoryImpl @Inject constructor(
    private val walkDao: WalkDao,
    private val gpsPointDao: GpsPointDao
) : WalkRepository {

    // ========================================
    // PUBLIC API (implements WalkRepository)
    // ========================================

    override suspend fun startWalk(): Walk {
        // Get current time
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        // Create a new walk entity
        val newWalkEntity = WalkEntity(
            id = 0, // Room will auto-generate this
            startTime = now.toEpochMillis(),
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            isActive = true,
            date = today.toDateString()
        )

        // Insert into database (returns the generated ID)
        val insertedId = walkDao.insertWalk(newWalkEntity)

        // Return the domain model with the actual ID
        return newWalkEntity.copy(id = insertedId).toDomainModel(emptyList())
    }

    override suspend fun stopWalk(totalSteps: Int, distanceMeters: Double): Walk? {
        // Get the currently active walk from database (one-time query)
        val activeWalkEntity = walkDao.getActiveWalkNow()
            ?: return null // No active walk to stop

        // Get current time for end time
        val now = LocalDateTime.now()

        // Update the walk with final values
        val updatedWalkEntity = activeWalkEntity.copy(
            endTime = now.toEpochMillis(),
            totalSteps = totalSteps,
            distanceMeters = distanceMeters,
            isActive = false
        )

        // Save to database
        walkDao.updateWalk(updatedWalkEntity)

        // Get GPS points for this walk (for the return value)
        val gpsPoints = gpsPointDao.getGpsPointsForWalkOnce(updatedWalkEntity.id)
            .map { it.toDomainModel() }

        // Return the updated domain model
        return updatedWalkEntity.toDomainModel(gpsPoints)
    }

    override fun getActiveWalk(): Flow<Walk?> {
        // Get Flow of WalkEntity from DAO
        return walkDao.getActiveWalk()
            .map { walkEntity ->
                // If no active walk, return null
                walkEntity?.toDomainModel(emptyList())

                // Note: We don't fetch GPS points here for performance
                // GPS points are only loaded when specifically needed (Paths screen)
                // During an active walk, we just need the walk metadata
            }
    }

    override fun getAllWalks(): Flow<List<Walk>> {
        return walkDao.getAllWalks()
            .map { walkEntities ->
                walkEntities.map { entity ->
                    // For list view, we don't need GPS points (performance)
                    entity.toDomainModel(emptyList())
                }
            }
    }

    override fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<Walk>> {
        return walkDao.getWalksByDateRange(startDate, endDate)
            .map { walkEntities ->
                walkEntities.map { entity ->
                    entity.toDomainModel(emptyList())
                }
            }
    }

    override suspend fun deleteAllWalks() {
        walkDao.deleteAllWalks()
    }

    // ========================================
    // PRIVATE HELPER FUNCTIONS (Mappers)
    // ========================================

    /**
     * Convert WalkEntity (database) to Walk (domain model).
     *
     * This is called a MAPPER function.
     * It translates between different representations of the same data.
     *
     * Key conversions:
     * - Long (Unix timestamp) → LocalDateTime (nice Kotlin date)
     * - String ("2025-10-23") → LocalDate (proper date object)
     */
    private fun WalkEntity.toDomainModel(gpsPoints: List<GpsPoint>): Walk {
        return Walk(
            id = this.id,
            startTime = this.startTime.toLocalDateTime(),
            endTime = this.endTime?.toLocalDateTime(),
            totalSteps = this.totalSteps,
            distanceMeters = this.distanceMeters,
            isActive = this.isActive,
            gpsPoints = gpsPoints,
            date = this.date.toLocalDate()
        )
    }

    /**
     * Convert Walk (domain model) to WalkEntity (database).
     *
     * Used when we need to update a walk in the database.
     */
    private fun Walk.toEntity(): WalkEntity {
        return WalkEntity(
            id = this.id,
            startTime = this.startTime.toEpochMillis(),
            endTime = this.endTime?.toEpochMillis(),
            totalSteps = this.totalSteps,
            distanceMeters = this.distanceMeters,
            isActive = this.isActive,
            date = this.date.toDateString()
        )
    }

    // ========================================
    // EXTENSION FUNCTIONS (Type Converters)
    // ========================================

    /**
     * Convert Unix timestamp (Long) to LocalDateTime.
     *
     * Example: 1729692000000 → 2025-10-23T14:00:00
     */
    private fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this),
            ZoneId.systemDefault()
        )
    }

    /**
     * Convert LocalDateTime to Unix timestamp (Long).
     *
     * Example: 2025-10-23T14:00:00 → 1729692000000
     */
    private fun LocalDateTime.toEpochMillis(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Convert String to LocalDate.
     *
     * Example: "2025-10-23" → LocalDate(2025, 10, 23)
     */
    private fun String.toLocalDate(): LocalDate {
        return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Convert LocalDate to String.
     *
     * Example: LocalDate(2025, 10, 23) → "2025-10-23"
     */
    private fun LocalDate.toDateString(): String {
        return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Convert GpsPointEntity (database) to GpsPoint (domain model).
     *
     * Key conversion: Long (Unix timestamp) → LocalDateTime
     */
    private fun com.example.stepeeeasy.data.local.entity.GpsPointEntity.toDomainModel(): GpsPoint {
        return GpsPoint(
            latitude = this.latitude,
            longitude = this.longitude,
            timestamp = this.timestamp.toLocalDateTime(),
            accuracy = this.accuracy
        )
    }
}
