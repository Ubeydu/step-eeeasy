package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * Use Case: Stop the currently active walk session.
 *
 * This represents the business action "user stops a walk".
 * Takes the final step count and distance, and persists them to the database.
 *
 * Currently simple, but can be extended with:
 * - Validation (ensure steps/distance are reasonable)
 * - Stop tracking service
 * - Calculate statistics
 * - Trigger notifications
 *
 * @param walkRepository Repository to persist walk data
 *
 * Example usage in ViewModel:
 * ```
 * viewModelScope.launch {
 *     val finalSteps = sensorManager.getCurrentSteps()
 *     val finalDistance = distanceCalculator.getDistance()
 *     val walk = stopWalkUseCase(finalSteps, finalDistance)
 *     _uiState.value = UiState.Idle
 * }
 * ```
 */
class StopWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    /**
     * Execute the use case: Stop the active walk.
     *
     * @param totalSteps Final step count from the sensor
     * @param distanceMeters Final distance calculated (in meters)
     * @return The updated Walk with end time and final values, or null if no active walk
     *
     * Note: Returns null if there's no active walk to stop
     * (This shouldn't happen in normal flow, but handled for safety)
     */
    suspend operator fun invoke(totalSteps: Int, distanceMeters: Double): Walk? {
        // Basic validation
        require(totalSteps >= 0) { "Step count cannot be negative" }
        require(distanceMeters >= 0) { "Distance cannot be negative" }

        // For now, just delegate to repository
        // In Phase 3 (sensor integration), we'll add:
        // - Stop tracking service
        // - Stop GPS recording
        // - Stop step counter
        return walkRepository.stopWalk(totalSteps, distanceMeters)
    }
}
