package com.example.stepeeeasy.data.repository

import com.example.stepeeeasy.data.local.dao.WalkDao
import com.example.stepeeeasy.data.local.entity.WalkEntity
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
 */
class WalkRepositoryImpl @Inject constructor(
    private val walkDao: WalkDao
) : WalkRepository {

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
        return newWalkEntity.copy(id = insertedId).toDomainModel()
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

        // Return the updated domain model
        return updatedWalkEntity.toDomainModel()
    }

    override fun getActiveWalk(): Flow<Walk?> {
        // Get Flow of WalkEntity from DAO
        return walkDao.getActiveWalk()
            .map { walkEntity ->
                // If no active walk, return null
                walkEntity?.toDomainModel()
            }
    }

    override fun getAllWalks(): Flow<List<Walk>> {
        return walkDao.getAllWalks()
            .map { walkEntities ->
                walkEntities.map { entity ->
                    entity.toDomainModel()
                }
            }
    }

    override fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<Walk>> {
        return walkDao.getWalksByDateRange(startDate, endDate)
            .map { walkEntities ->
                walkEntities.map { entity ->
                    entity.toDomainModel()
                }
            }
    }

    override suspend fun deleteAllWalks() {
        walkDao.deleteAllWalks()
    }

    private fun WalkEntity.toDomainModel(): Walk {
        return Walk(
            id = this.id,
            startTime = this.startTime.toLocalDateTime(),
            endTime = this.endTime?.toLocalDateTime(),
            totalSteps = this.totalSteps,
            distanceMeters = this.distanceMeters,
            isActive = this.isActive,
            date = this.date.toLocalDate()
        )
    }

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

    private fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this),
            ZoneId.systemDefault()
        )
    }

    private fun LocalDateTime.toEpochMillis(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun String.toLocalDate(): LocalDate {
        return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun LocalDate.toDateString(): String {
        return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
