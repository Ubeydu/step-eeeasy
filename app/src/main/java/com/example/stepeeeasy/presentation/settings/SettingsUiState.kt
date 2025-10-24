package com.example.stepeeeasy.presentation.settings

/**
 * UI state for Settings screen.
 *
 * This is what the ViewModel exposes to the Composable.
 * The UI reads these values and calls ViewModel functions when user interacts.
 *
 * @param heightInput User's input for height (String to allow empty state while typing)
 * @param activityRecognitionEnabled Current state of Activity Recognition toggle
 * @param errorMessage Error message to show (e.g., "Height must be between 50-250 cm"), null if no error
 * @param showClearDialog Whether to show "Clear Recorded Walks" confirmation dialog
 * @param showSuccessSnackbar Whether to show success snackbar after saving height
 */
data class SettingsUiState(
    val heightInput: String = "",
    val activityRecognitionEnabled: Boolean = false,
    val errorMessage: String? = null,
    val showClearDialog: Boolean = false,
    val showSuccessSnackbar: Boolean = false
)
