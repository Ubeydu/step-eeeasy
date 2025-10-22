package com.example.stepeeeasy.domain.model

import java.time.LocalDate

data class DailyStats(
    val date: LocalDate,
    val totalSteps: Int,
    val totalDistanceMeters: Double,
    val walkCount: Int = 0
) {
    val totalDistanceKm: Double
        get() = totalDistanceMeters / 1000.0
}
