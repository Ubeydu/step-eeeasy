package com.example.stepeeeasy.data.repository

import com.example.stepeeeasy.data.local.datastore.SettingsDataStore
import com.example.stepeeeasy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of SettingsRepository using DataStore.
 *
 * Wraps SettingsDataStore and adds business logic:
 * - Height validation (50-250 cm range)
 * - Clear settings functionality
 *
 * @param settingsDataStore DataStore wrapper (injected by Hilt)
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    companion object {
        private const val MIN_HEIGHT_CM = 50
        private const val MAX_HEIGHT_CM = 250
        private const val DEFAULT_HEIGHT_CM = 170
    }

    override val userHeightCm: Flow<Int> = settingsDataStore.userHeightCm

    override val activityRecognitionEnabled: Flow<Boolean> = settingsDataStore.activityRecognitionEnabled

    /**
     * Save user height with validation.
     *
     * Validates height is in acceptable range (50-250 cm).
     * Returns failure with descriptive message if invalid.
     */
    override suspend fun saveUserHeight(heightCm: Int): Result<Unit> {
        return if (heightCm in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            settingsDataStore.saveUserHeight(heightCm)
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalArgumentException(
                    "Height must be between $MIN_HEIGHT_CM and $MAX_HEIGHT_CM cm"
                )
            )
        }
    }

    /**
     * Save Activity Recognition permission status.
     * No validation needed - boolean is always valid.
     */
    override suspend fun saveActivityRecognitionEnabled(enabled: Boolean) {
        settingsDataStore.saveActivityRecognitionEnabled(enabled)
    }

    /**
     * Clear all settings by resetting to defaults.
     * - Height: 170 cm (average)
     * - Activity Recognition: false (disabled)
     */
    override suspend fun clearAllSettings() {
        settingsDataStore.saveUserHeight(DEFAULT_HEIGHT_CM)
        settingsDataStore.saveActivityRecognitionEnabled(false)
    }
}
