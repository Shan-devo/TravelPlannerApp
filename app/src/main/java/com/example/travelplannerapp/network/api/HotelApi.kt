package com.example.travelplannerapp.network.api

import com.example.travelplannerapp.network.model.HotelSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HotelApi {

    @GET("v1/reference-data/locations/hotels/by-city")
    suspend fun getHotelsByCity(
        @Query("cityCode") cityCode: String,
        @Query("radius") radius: Int = 5,
        @Query("radiusUnit") radiusUnit: String = "KM"
    ): HotelSearchResponse
}
