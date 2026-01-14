package com.example.travelplannerapp.network.model

data class Booking(
    val id: String,
    val hotelName: String,
    val city: String,
    val checkInDate: String,
    val checkOutDate: String,
    val bookedAt: Long = System.currentTimeMillis()
)
