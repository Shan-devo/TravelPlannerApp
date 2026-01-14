package com.example.travelplannerapp.network.model

data class Hotel(
    val hotelId: String,
    val name: String,
    val geoCode: GeoCode,
    val address: Address
)
