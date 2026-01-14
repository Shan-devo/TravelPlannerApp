package com.example.travelplannerapp.network

import com.example.travelplannerapp.network.api.HotelApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HotelServiceBuilder {

    private const val BASE_URL = "https://test.api.amadeus.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            AmadeusAuthInterceptor {
                TokenManager.getToken()
            }
        )
        .build()

    val api: HotelApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HotelApi::class.java)
}
