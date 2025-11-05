package com.example.stepeeeasy.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Manages step counting using device's step counter sensor.
 */
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

    fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null
    }

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

    fun stopTracking(): Int {
        if (isTracking) {
            sensorManager.unregisterListener(this)
            isTracking = false
            onStepCountChanged = null
            Log.d(TAG, "Step tracking stopped. Final steps: $currentSteps")
        }
        return currentSteps
    }

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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    fun getCurrentSteps(): Int {
        return currentSteps
    }
}
