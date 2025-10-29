package com.example.stepeeeasy.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
// * Manages step counting using the device's built-in step counter sensor.
// *
// * Key concepts:
// * - Calculates delta: current_steps - baseline_steps
// * - Notifies callback with step updates
// * - Uses Sensor.TYPE_STEP_COUNTER (cumulative steps since last reboot)
// * - Records baseline when walk starts
// */
class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var baselineSteps: Int = 0
    private var currentSteps: Int = 0
    private var isTracking: Boolean = false

    private var onStepCountChanged: ((Int) -> Unit)? = null

    companion object {
        private const val TAG = "StepCounterManager"
    }

    /**
     * Checks if the device has a step counter sensor.
     *
     * @return true if sensor is available, false otherwise
     */
    fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null
    }

    /**
     * Starts tracking steps. Records the current step count as baseline.
     *
     * @param onStepCountChanged Callback invoked when step count changes
     */
    fun startTracking(onStepCountChanged: (Int) -> Unit) {
        if (stepCounterSensor == null) {
            Log.e(TAG, "Step counter sensor not available on this device")
            return
        }

        this.onStepCountChanged = onStepCountChanged
        this.isTracking = true
        this.baselineSteps = 0 // Will be set on first sensor event
        this.currentSteps = 0

        // Register listener with SENSOR_DELAY_UI (good balance for UI updates)
        sensorManager.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        Log.d(TAG, "Step tracking started")
    }

    /**
     * Stops tracking steps and unregisters the sensor listener.
     *
     * @return Final step count for the walk
     */
    fun stopTracking(): Int {
        if (isTracking) {
            sensorManager.unregisterListener(this)
            isTracking = false
            onStepCountChanged = null
            Log.d(TAG, "Step tracking stopped. Final steps: $currentSteps")
        }
        return currentSteps
    }

    /**
     * Called when sensor values change. Calculates step delta and notifies callback.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && isTracking) {
            val totalStepsSinceReboot = event.values[0].toInt()

            // Set baseline on first reading
            if (baselineSteps == 0) {
                baselineSteps = totalStepsSinceReboot
                Log.d(TAG, "Baseline set: $baselineSteps")
            }

            // Calculate delta (steps during this walk)
            currentSteps = totalStepsSinceReboot - baselineSteps

            // Notify callback
            onStepCountChanged?.invoke(currentSteps)
        }
    }

    /**
     * Called when sensor accuracy changes. Not used for step counter.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    /**
     * Gets the current step count without stopping tracking.
     *
     * @return Current step count since walk started
     */
    fun getCurrentSteps(): Int {
        return currentSteps
    }
}
