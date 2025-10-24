package com.example.stepeeeasy.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings.
 *
 * Defines contract for settings operations. Implementation lives in data layer.
 * This follows Clean Architecture - domain layer defines what operations exist,
 * data layer implements how they work (using DataStore, SharedPreferences, etc.)
 */
interface SettingsRepository {
    /**
     * Get user height in centimeters as Flow.
     * Emits new values whenever height changes.
     * Default: 170 cm
     */
    val userHeightCm: Flow<Int>

    /**
     * Get Activity Recognition permission status as Flow.
     * Emits new values whenever status changes.
     * Default: false
     */
    val activityRecognitionEnabled: Flow<Boolean>

    /**
     * Save user height with validation.
     *
     * @param heightCm User height in centimeters (must be 50-250)
     * @return Result.success if valid and saved, Result.failure with error message if invalid
     */
    suspend fun saveUserHeight(heightCm: Int): Result<Unit>

    /**
     * Save Activity Recognition permission status.
     *
     * @param enabled Whether Activity Recognition is enabled
     */
    suspend fun saveActivityRecognitionEnabled(enabled: Boolean)

    /**
     * Clear all settings and reset to defaults.
     * Useful for testing and "reset to defaults" feature.
     */
    suspend fun clearAllSettings()
}
