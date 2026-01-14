package com.example.travelplannerapp.repository

import com.example.travelplannerapp.network.model.Hotel

class HotelRepository {
    suspend fun searchHotels(cityCode: String): List<Hotel> {
        return HotelServiceBuilder.api
            .getHotelsByCity(cityCode)
            .data
    }
}
