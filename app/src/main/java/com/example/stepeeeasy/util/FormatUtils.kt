package com.example.stepeeeasy.util

/**
 * Utility functions for formatting data for UI display.
 */
object FormatUtils {

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

    fun formatDistanceFromMeters(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            "%.1f km".format(distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }
    }

    fun formatSteps(steps: Int): String {
        return "%,d".format(steps)
    }

    fun formatElapsedTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, secs)
    }
}
