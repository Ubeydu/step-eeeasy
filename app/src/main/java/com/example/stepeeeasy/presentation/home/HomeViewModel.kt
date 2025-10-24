package com.example.stepeeeasy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.usecase.StartWalkUseCase
import com.example.stepeeeasy.domain.usecase.StopWalkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Responsibilities:
 * - Manage walk state (idle vs active)
 * - Handle START/STOP button clicks
 * - Manage timer (count elapsed seconds)
 * - Expose UI state to Composable
 *
 * @HiltViewModel tells Hilt to generate a ViewModelFactory for this
 * Hilt will automatically inject the use cases
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startWalkUseCase: StartWalkUseCase,
    private val stopWalkUseCase: StopWalkUseCase
) : ViewModel() {

    // ========================================
    // UI State Management
    // ========================================

    /**
     * UI State holder.
     *
     * This is the single source of truth for the Home screen UI.
     * When this changes, the Composable will automatically recompose.
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Timer job for updating elapsed time.
     * We keep a reference so we can cancel it when stopping the walk.
     */
    private var timerJob: Job? = null

    // ========================================
    // User Actions (called from UI)
    // ========================================

    /**
     * User tapped the START button.
     *
     * Creates a new walk in the database and starts the timer.
     */
    fun onStartWalkClicked() {
        viewModelScope.launch {
            try {
                // Call use case to start walk
                val walk = startWalkUseCase()

                // Update UI to show active walk
                _uiState.value = HomeUiState.WalkActive(
                    walk = walk,
                    elapsedSeconds = 0,
                    currentSteps = 0,
                    currentDistanceMeters = 0.0
                )

                // Start the timer
                startTimer(walk.startTime)

            } catch (e: Exception) {
                // Handle error (for now, just log)
                // In Phase 3, we'll show error messages to user
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start walk")
            }
        }
    }

    /**
     * User tapped the STOP button.
     *
     * Stops the timer and saves the walk with final values.
     */
    fun onStopWalkClicked() {
        viewModelScope.launch {
            try {
                // Stop the timer first
                timerJob?.cancel()

                // Get current state
                val currentState = _uiState.value
                if (currentState !is HomeUiState.WalkActive) {
                    // Should not happen, but handle gracefully
                    return@launch
                }

                // Call use case to stop walk
                val stoppedWalk = stopWalkUseCase(
                    totalSteps = currentState.currentSteps,
                    distanceMeters = currentState.currentDistanceMeters
                )

                // Update UI back to idle state
                _uiState.value = HomeUiState.Idle

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to stop walk")
            }
        }
    }

    // ========================================
    // Timer Logic
    // ========================================

    /**
     * Start a timer that updates elapsed seconds every second.
     *
     * This creates a coroutine that runs in a loop:
     * 1. Calculate elapsed time since walk started
     * 2. Update UI state with new elapsed seconds
     * 3. Wait 1 second
     * 4. Repeat
     *
     * @param startTime When the walk started (from database)
     */
    private fun startTimer(startTime: LocalDateTime) {
        timerJob = viewModelScope.launch {
            while (true) {
                // Calculate elapsed seconds
                val now = LocalDateTime.now()
                val elapsed = Duration.between(startTime, now).seconds

                // Update UI state with new elapsed time
                val currentState = _uiState.value
                if (currentState is HomeUiState.WalkActive) {
                    _uiState.value = currentState.copy(elapsedSeconds = elapsed)
                }

                // Wait 1 second before next update
                delay(1000)
            }
        }
    }

    // ========================================
    // Cleanup
    // ========================================

    /**
     * Cancel timer when ViewModel is destroyed.
     *
     * This is called when the user navigates away from Home screen.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * UI State sealed class.
 *
 * Represents all possible states the Home screen can be in.
 * Sealed class means the compiler knows all possible states.
 *
 * Why sealed class?
 * - Type-safe state management
 * - Compiler ensures you handle all cases (in when expressions)
 * - Clear definition of what data is available in each state
 */
sealed class HomeUiState {
    /**
     * Idle state: No active walk.
     * Shows START button, no timer.
     */
    data object Idle : HomeUiState()

    /**
     * Active walk state: Walk is in progress.
     * Shows STOP button, running timer, live stats.
     *
     * @param walk The active walk from database
     * @param elapsedSeconds Seconds since walk started (for timer display)
     * @param currentSteps Current step count (from sensor - Phase 3)
     * @param currentDistanceMeters Current distance in meters (calculated - Phase 3)
     */
    data class WalkActive(
        val walk: Walk,
        val elapsedSeconds: Long,
        val currentSteps: Int,
        val currentDistanceMeters: Double
    ) : HomeUiState() {
        /**
         * Format elapsed time as HH:MM:SS.
         *
         * Examples:
         * - 65 seconds → "00:01:05"
         * - 3661 seconds → "01:01:01"
         */
        val formattedTime: String
            get() {
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                return "%02d:%02d:%02d".format(hours, minutes, seconds)
            }

        /**
         * Format distance in kilometers with 2 decimal places.
         *
         * Example: 4235.5 meters → "4.24 km"
         */
        val formattedDistance: String
            get() = "%.2f km".format(currentDistanceMeters / 1000.0)
    }

    /**
     * Error state: Something went wrong.
     * Shows error message to user.
     */
    data class Error(val message: String) : HomeUiState()
}
