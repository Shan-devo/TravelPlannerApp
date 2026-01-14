package com.example.travelplannerapp.network

import com.example.travelplannerapp.network.api.UnsplashApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UnsplashService {

    private const val BASE_URL = "https://api.unsplash.com/"

    val api: UnsplashApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UnsplashApi::class.java)
}
