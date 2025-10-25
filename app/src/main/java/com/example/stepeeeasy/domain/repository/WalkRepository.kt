package com.example.stepeeeasy.domain.repository

import com.example.stepeeeasy.domain.model.Walk
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Walk operations.
 *
 * This is a CONTRACT that defines what operations are available for managing walks.
 * The actual implementation lives in the data layer (WalkRepositoryImpl).
 *
 * Why an interface?
 * - Dependency Injection: ViewModels depend on the interface, not the concrete implementation
 * - Testing: Easy to create fake implementations for unit tests
 * - Flexibility: Can swap implementations without changing ViewModels
 */
interface WalkRepository {

    /**
     * Start a new walk session.
     *
     * Creates a new walk record in the database with:
     * - startTime = now
     * - isActive = true
     * - All other fields initialized to defaults
     *
     * @return The newly created Walk object with database-generated ID
     *
     * Example usage in ViewModel:
     * ```
     * viewModelScope.launch {
     *     val newWalk = walkRepository.startWalk()
     *     println("Started walk with ID: ${newWalk.id}")
     * }
     * ```
     */
    suspend fun startWalk(): Walk

    /**
     * Stop the currently active walk.
     *
     * Updates the active walk with:
     * - endTime = now
     * - isActive = false
     * - Final step count and distance (passed as parameters)
     *
     * @param totalSteps Final step count from sensor
     * @param distanceMeters Final distance calculated
     * @return The updated Walk object, or null if no active walk exists
     *
     * Example:
     * ```
     * val stoppedWalk = walkRepository.stopWalk(totalSteps = 5432, distanceMeters = 4200.0)
     * ```
     */
    suspend fun stopWalk(totalSteps: Int, distanceMeters: Double): Walk?

    /**
     * Get the currently active walk (if any).
     *
     * Returns a Flow so the UI automatically updates when the walk changes.
     * This is reactive - when you update the walk in the database, the UI sees it immediately.
     *
     * @return Flow that emits the active walk, or null if no walk is active
     *
     * Example in ViewModel:
     * ```
     * val activeWalk: StateFlow<Walk?> = walkRepository.getActiveWalk()
     *     .stateIn(viewModelScope, SharingStarted.Lazily, null)
     * ```
     */
    fun getActiveWalk(): Flow<Walk?>

    /**
     * Get all walks, sorted by start time (newest first).
     *
     * @return Flow of all walks (updates automatically when walks are added/removed)
     *
     * Used in History screen to display walk list.
     */
    fun getAllWalks(): Flow<List<Walk>>

    /**
     * Get walks within a specific date range.
     *
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     * @return Flow of walks in that date range
     *
     * Used for weekly views in History screen.
     */
    fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<Walk>>

    /**
     * Delete all walks from the database.
     *
     * Used for the "Clear Recorded Walks" feature in Settings.
     */
    suspend fun deleteAllWalks()
}
