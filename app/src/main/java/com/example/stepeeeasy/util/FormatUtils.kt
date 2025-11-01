package com.example.stepeeeasy.util

/**
 * Utility functions for formatting data for UI display.
 *
 * Keeps presentation logic separate from business logic and UI code.
 */
object FormatUtils {

    /**
     * Formats distance for display with appropriate units.
     *
     * @param distanceKm Distance in kilometers
     * @return Formatted string with appropriate unit
     *
     * Examples:
     * - 0.024 km (24m) → "24 m"
     * - 1.055 km (1055m) → "1 km 55 m"
     * - 2.0 km (2000m) → "2 km"
     * - 12.5 km → "12.5 km"
     */
    fun formatDistance(distanceKm: Double): String {
        // Convert to meters for easier calculation
        val meters = (distanceKm * 1000).toInt()

        // Less than 1 km: show in meters
        if (meters < 1000) {
            return "$meters m"
        }

        // Less than 10 km: show km and remaining meters
        if (meters < 10000) {
            val km = meters / 1000
            val remainingMeters = meters % 1000

            // If no remaining meters, just show km
            if (remainingMeters == 0) {
                return "$km km"
            }
            // Otherwise show both
            return "$km km $remainingMeters m"
        }

        // 10 km or more: show km with decimal
        return "%.1f km".format(distanceKm)
    }

    /**
     * Formats distance in meters for display.
     * Converts to kilometers if >= 1000 meters, otherwise shows meters.
     *
     * @param distanceMeters Distance in meters
     * @return Formatted string with appropriate unit
     *
     * Examples:
     * - 7525 meters → "7.5 km"
     * - 850 meters → "850 m"
     * - 50 meters → "50 m"
     */
    fun formatDistanceFromMeters(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            "%.1f km".format(distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }
    }

    /**
     * Formats step count with comma separators for readability.
     *
     * @param steps Number of steps
     * @return Formatted string with commas
     *
     * Examples:
     * - 1234 → "1,234"
     * - 10000 → "10,000"
     * - 567 → "567"
     */
    fun formatSteps(steps: Int): String {
        return "%,d".format(steps)
    }

    /**
     * Formats elapsed time as HH:MM:SS.
     *
     * @param seconds Total elapsed seconds
     * @return Formatted time string
     *
     * Examples:
     * - 65 seconds → "00:01:05"
     * - 3661 seconds → "01:01:01"
     * - 45 seconds → "00:00:45"
     */
    fun formatElapsedTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, secs)
    }
}
