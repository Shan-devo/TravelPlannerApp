package com.example.travelplannerapp.network.api

import com.example.travelplannerapp.network.model.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String,
        @Query("per_page") perPage: Int = 1
    ): UnsplashResponse
}
