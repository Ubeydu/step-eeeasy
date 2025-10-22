package com.example.stepeeeasy.domain.model

import java.time.LocalDateTime

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,
    val accuracy: Float = 0f
)
