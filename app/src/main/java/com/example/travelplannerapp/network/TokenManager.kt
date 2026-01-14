package com.example.travelplannerapp.network

import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenManager {

    private const val BASE_URL = "https://test.api.amadeus.com/"
    private const val API_KEY = "MdNAvo1tSD2MR4rKrYq3NVkILWeAvJ75"
    private const val API_SECRET = "3OPSUhGC1L33d8AG"

    private var token: String? = null
    private var expiryTime: Long = 0

    suspend fun getToken(): String {
        val now = System.currentTimeMillis()

        return if (token == null || now >= expiryTime) {
            fetchNewToken()
        } else {
            token!!
        }
    }

    private suspend fun fetchNewToken(): String {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AmadeusTokenService::class.java)

        val response = service.getToken(
            apiKey = API_KEY,
            apiSecret = API_SECRET
        )

        token = response.access_token
        expiryTime = System.currentTimeMillis() + (response.expires_in * 1000)

        return token!!
    }
}
