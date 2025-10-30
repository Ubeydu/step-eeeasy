package com.example.stepeeeasy.util

/**
 * Utility object for calculating stride length and distance based on user height.
 *
 * Stride length formula:
 * - Average stride length is approximately 43% of a person's height
 * - stride_length_meters = height_cm × 0.43 / 100.0
 *
 * Distance formula:
 * - distance_meters = steps × stride_length_meters
 * - distance_km = distance_meters / 1000.0
 */
object StrideCalculator {

    private const val STRIDE_LENGTH_RATIO = 0.43

    /**
     * Calculates stride length in meters based on user height in centimeters.
     *
     * @param heightCm User's height in centimeters (typical range: 50-250 cm)
     * @return Stride length in meters
     *
     * Example: For a person 175 cm tall:
     * - stride_length = 175 × 0.43 / 100 = 0.7525 meters (75.25 cm)
     */
    fun calculateStrideLengthMeters(heightCm: Int): Double {
        return (heightCm * STRIDE_LENGTH_RATIO) / 100.0
    }

    /**
     * Calculates total distance in kilometers based on steps and user height.
     *
     * @param steps Number of steps taken
     * @param heightCm User's height in centimeters
     * @return Distance in kilometers (precise value, e.g., 7.525)
     *
     * Example: 10,000 steps for a person 175 cm tall:
     * - stride_length = 0.7525 meters
     * - distance = 10,000 × 0.7525 / 1000 = 7.525 km
     */
    fun calculateDistanceKm(steps: Int, heightCm: Int): Double {
        val strideLengthMeters = calculateStrideLengthMeters(heightCm)
        val distanceMeters = steps * strideLengthMeters
        return distanceMeters / 1000.0
    }

    /**
     * Calculates total distance in meters (useful for database storage).
     *
     * @param steps Number of steps taken
     * @param heightCm User's height in centimeters
     * @return Distance in meters (precise value)
     */
    fun calculateDistanceMeters(steps: Int, heightCm: Int): Double {
        val strideLengthMeters = calculateStrideLengthMeters(heightCm)
        return steps * strideLengthMeters
    }
}
