package com.example.stepeeeasy.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

data class Walk(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val totalSteps: Int,
    val distanceMeters: Double,
    val isActive: Boolean = false,
    val date: LocalDate
) {
    val durationSeconds: Long
        get() = if (endTime != null) {
            Duration.between(startTime, endTime).seconds
        } else {
            0L
        }

    val distanceKm: Double
        get() = distanceMeters / 1000.0
}
