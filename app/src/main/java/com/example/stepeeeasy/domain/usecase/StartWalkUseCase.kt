package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * Use Case: Start a new walk session.
 *
 * This represents the business action "user starts a walk".
 * Currently it's a thin wrapper, but can be extended with:
 * - Permission checks
 * - Sensor availability validation
 * - Preventing multiple active walks
 * - Business rule enforcement
 *
 * Why Use Cases?
 * - Encapsulates business logic (separate from UI and data layers)
 * - Single responsibility (one action per class)
 * - Easy to test in isolation
 * - Clear naming shows user intent
 *
 * @param walkRepository Repository to persist walk data
 *
 * Example usage in ViewModel:
 * ```
 * viewModelScope.launch {
 *     val walk = startWalkUseCase()
 *     _uiState.value = UiState.WalkActive(walk)
 * }
 * ```
 */
class StartWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    /**
     * Execute the use case: Start a new walk.
     *
     * The `operator fun invoke()` pattern allows calling this like a function:
     * `startWalkUseCase()` instead of `startWalkUseCase.execute()`
     *
     * @return The newly created Walk with database-generated ID
     *
     * Explanation:
     * - suspend: This is a coroutine function (non-blocking)
     * - operator: Allows using () syntax
     * - invoke(): Kotlin special function for function-like objects
     */
    suspend operator fun invoke(): Walk {
        // For now, just delegate to repository
        // In Phase 3 (sensor integration), we'll add:
        // - Permission checks
        // - Check if walk already active
        // - Start tracking service
        return walkRepository.startWalk()
    }
}
