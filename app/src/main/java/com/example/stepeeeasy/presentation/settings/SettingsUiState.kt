package com.example.stepeeeasy.presentation.settings

/**
 * UI state for Settings screen.
 *
 * This is what the ViewModel exposes to the Composable.
 * The UI reads these values and calls ViewModel functions when user interacts.
 *
 * @param heightInput User's draft input for height (String to allow empty state while typing)
 * @param savedHeight The actual saved height value from database (what's persisted)
 * @param activityRecognitionEnabled Current state of Activity Recognition toggle
 * @param errorMessage Error message to show (e.g., "Height must be between 50-250 cm"), null if no error
 * @param showClearDialog Whether to show "Clear Recorded Walks" confirmation dialog
 * @param heightSavedEvent Counter that increments each time height is saved (triggers snackbar)
 * @param walksClearedEvent Counter that increments each time walks are cleared (triggers snackbar)
 */
data class SettingsUiState(
    val heightInput: String = "",
    val savedHeight: Int = 0,
    val activityRecognitionEnabled: Boolean = false,
    val errorMessage: String? = null,
    val showClearDialog: Boolean = false,
    val heightSavedEvent: Int = 0,
    val walksClearedEvent: Int = 0
)
