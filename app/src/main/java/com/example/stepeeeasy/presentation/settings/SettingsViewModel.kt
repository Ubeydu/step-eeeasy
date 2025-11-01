package com.example.stepeeeasy.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.repository.SettingsRepository
import com.example.stepeeeasy.domain.usecase.ClearAllWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 *
 * Manages settings state and handles user interactions:
 * - User height input and validation
 * - Activity Recognition toggle
 * - Clear walks functionality with confirmation dialog
 *
 * @param settingsRepository Repository for settings persistence
 * @param clearAllWalksUseCase Use case for clearing walk history
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val clearAllWalksUseCase: ClearAllWalksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load initial settings from repository
        loadSettings()
    }

    /**
     * Load current settings from repository.
     * Observes height and activity recognition status.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Observe saved user height from database
            settingsRepository.userHeightCm.collect { height ->
                _uiState.update { it.copy(savedHeight = height) }
            }
        }

        viewModelScope.launch {
            // Observe activity recognition status
            settingsRepository.activityRecognitionEnabled.collect { enabled ->
                _uiState.update { it.copy(activityRecognitionEnabled = enabled) }
            }
        }
    }

    /**
     * Handle height input change.
     * Updates UI state but doesn't save yet (validation happens on save).
     *
     * @param newHeight New height input from TextField
     */
    fun onHeightChanged(newHeight: String) {
        _uiState.update { it.copy(heightInput = newHeight, errorMessage = null) }
    }

    /**
     * Handle save button click for height.
     * Validates input and saves to repository if valid.
     */
    fun onSaveHeight() {
        val heightInput = _uiState.value.heightInput

        // Try to parse height as integer
        val height = heightInput.toIntOrNull()

        if (height == null) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid number") }
            return
        }

        // Save to repository (which will validate range 50-250)
        viewModelScope.launch {
            val result = settingsRepository.saveUserHeight(height)

            result.onFailure { error ->
                // Show validation error from repository
                _uiState.update { it.copy(errorMessage = error.message) }
            }.onSuccess {
                // Clear error, clear input field, and trigger snackbar
                _uiState.update { it.copy(
                    heightInput = "",
                    errorMessage = null,
                    heightSavedEvent = it.heightSavedEvent + 1
                ) }
            }
        }
    }

    /**
     * Handle Activity Recognition toggle change.
     * Saves immediately (no validation needed for boolean).
     *
     * @param enabled New toggle state
     */
    fun onActivityRecognitionToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveActivityRecognitionEnabled(enabled)
        }
    }

    /**
     * Handle "Clear Recorded Walks" button click.
     * Shows confirmation dialog instead of deleting immediately.
     */
    fun onClearWalksClicked() {
        _uiState.update { it.copy(showClearDialog = true) }
    }

    /**
     * Handle confirmation dialog dismiss.
     * Hides the dialog without deleting.
     */
    fun onDismissDialog() {
        _uiState.update { it.copy(showClearDialog = false) }
    }

    /**
     * Handle confirmation dialog confirm.
     * Deletes all walks and closes dialog.
     */
    fun onConfirmClearWalks() {
        viewModelScope.launch {
            clearAllWalksUseCase()
            _uiState.update { it.copy(
                showClearDialog = false,
                walksClearedEvent = it.walksClearedEvent + 1
            ) }
        }
    }
}
