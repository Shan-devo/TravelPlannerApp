package com.example.travelplannerapp.data

data class FavoriteRoute(
    val id: Long = 0,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    var destinationName: String,
    val distanceKm: Double,
    val durationMin: Int
)
