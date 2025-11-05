package com.example.stepeeeasy.util

/**
 * Calculates stride length and distance based on user height.
 * Assumes stride length is 43% of height.
 */
object StrideCalculator {

    private const val STRIDE_LENGTH_RATIO = 0.43

    fun calculateStrideLengthMeters(heightCm: Int): Double {
        return (heightCm * STRIDE_LENGTH_RATIO) / 100.0
    }

    fun calculateDistanceKm(steps: Int, heightCm: Int): Double {
        val strideLengthMeters = calculateStrideLengthMeters(heightCm)
        val distanceMeters = steps * strideLengthMeters
        return distanceMeters / 1000.0
    }

    fun calculateDistanceMeters(steps: Int, heightCm: Int): Double {
        val strideLengthMeters = calculateStrideLengthMeters(heightCm)
        return steps * strideLengthMeters
    }
}
