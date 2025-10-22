package com.example.stepeeeasy.data.local.model

/**
 * Data Transfer Object for DailyStats query results.
 * This is NOT an @Entity - it's used to hold aggregated query results from the walks table.
 */
data class DailyStatsDto(
    val date: String,
    val totalSteps: Int,
    val totalDistanceMeters: Double,
    val walkCount: Int
)
