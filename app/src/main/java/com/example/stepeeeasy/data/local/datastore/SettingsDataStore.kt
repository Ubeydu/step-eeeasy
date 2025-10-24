package com.example.stepeeeasy.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore wrapper for app settings.
 *
 * Provides type-safe access to user preferences stored in DataStore.
 * Uses Kotlin Flow for reactive updates.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    /**
     * Preference keys for settings.
     */
    private object PreferencesKeys {
        val USER_HEIGHT_CM = intPreferencesKey("user_height_cm")
        val ACTIVITY_RECOGNITION_ENABLED = booleanPreferencesKey("activity_recognition_enabled")
    }

    /**
     * Get user height in centimeters.
     * Default: 170 cm (average height)
     */
    val userHeightCm: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_HEIGHT_CM] ?: 170
    }

    /**
     * Get activity recognition permission status.
     * Default: false (not enabled)
     */
    val activityRecognitionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVITY_RECOGNITION_ENABLED] ?: false
    }

    /**
     * Save user height.
     * @param heightCm User height in centimeters (50-250 range)
     */
    suspend fun saveUserHeight(heightCm: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_HEIGHT_CM] = heightCm
        }
    }

    /**
     * Save activity recognition permission status.
     * @param enabled Whether Activity Recognition is enabled
     */
    suspend fun saveActivityRecognitionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVITY_RECOGNITION_ENABLED] = enabled
        }
    }
}
