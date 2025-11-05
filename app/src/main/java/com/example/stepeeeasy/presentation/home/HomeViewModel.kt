package com.example.stepeeeasy.presentation.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.SettingsRepository
import com.example.stepeeeasy.domain.usecase.StartWalkUseCase
import com.example.stepeeeasy.domain.usecase.StopWalkUseCase
import com.example.stepeeeasy.service.StepCounterManager
import com.example.stepeeeasy.util.StrideCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val stopWalkUseCase: StopWalkUseCase,
    private val settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModel() {

    private val stepCounterManager = StepCounterManager(application)
    private var userHeight: Int = 170 // Default, will be loaded from settings

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _walkStoppedEvent = MutableStateFlow(0)
    val walkStoppedEvent: StateFlow<Int> = _walkStoppedEvent.asStateFlow()

    private var timerJob: Job? = null

    /**
     * User tapped the START button.
     *
     * Creates a new walk in the database and starts the timer and step counter.
     */
    fun onStartWalkClicked() {
        viewModelScope.launch {
            try {
                // Load user height from settings
                userHeight = settingsRepository.userHeightCm.first()

                // Check if step counter sensor is available
                if (!stepCounterManager.isSensorAvailable()) {
                    _uiState.value = HomeUiState.Error("Step counter sensor not available on this device")
                    return@launch
                }

                // Call use case to start walk
                val walk = startWalkUseCase()

                // Update UI to show active walk
                _uiState.value = HomeUiState.WalkActive(
                    walk = walk,
                    elapsedSeconds = 0,
                    currentSteps = 0,
                    currentDistanceMeters = 0.0
                )

                // Start the step counter
                stepCounterManager.startTracking { steps ->
                    onStepsUpdated(steps)
                }

                // Start the timer
                startTimer(walk.startTime)

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start walk")
            }
        }
    }

    /**
     * User tapped the STOP button.
     *
     * Stops the timer, step counter, and saves the walk with final values.
     */
    fun onStopWalkClicked() {
        viewModelScope.launch {
            try {
                // Stop the timer first
                timerJob?.cancel()

                // Stop the step counter and get final steps
                val finalSteps = stepCounterManager.stopTracking()

                // Get current state
                val currentState = _uiState.value
                if (currentState !is HomeUiState.WalkActive) {
                    // Should not happen, but handle gracefully
                    return@launch
                }

                // Calculate final distance
                val finalDistanceMeters = StrideCalculator.calculateDistanceMeters(finalSteps, userHeight)

                // Call use case to stop walk
                val stoppedWalk = stopWalkUseCase(
                    totalSteps = finalSteps,
                    distanceMeters = finalDistanceMeters
                )

                // Update UI back to idle state
                _uiState.value = HomeUiState.Idle

                // Trigger snackbar
                _walkStoppedEvent.value = _walkStoppedEvent.value + 1

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to stop walk")
            }
        }
    }

    private fun onStepsUpdated(steps: Int) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.WalkActive) {
            val distanceMeters = StrideCalculator.calculateDistanceMeters(steps, userHeight)
            _uiState.value = currentState.copy(
                currentSteps = steps,
                currentDistanceMeters = distanceMeters
            )
        }
    }

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
     * Cancel timer and stop step counter when ViewModel is destroyed.
     *
     * This is called when the user navigates away from Home screen.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stepCounterManager.stopTracking()
    }
}

sealed class HomeUiState {
    data object Idle : HomeUiState()

    data class WalkActive(
        val walk: Walk,
        val elapsedSeconds: Long,
        val currentSteps: Int,
        val currentDistanceMeters: Double
    ) : HomeUiState() {
        val formattedTime: String
            get() {
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                return "%02d:%02d:%02d".format(hours, minutes, seconds)
            }
    }

    data class Error(val message: String) : HomeUiState()
}
