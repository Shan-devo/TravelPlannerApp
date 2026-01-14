package com.example.travelplannerapp.network

import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenManager {

    private const val BASE_URL = "https://test.api.amadeus.com/"

    private const val API_KEY = "MdNAvo1tSD2MR4rKrYq3NVkILWeAvJ75"
    private const val API_SECRET = "3OPSUhGC1L33d8AG"

    private var token: String? = null

    fun getToken(): String {
        if (token == null) {
            runBlocking {
                token = fetchToken()
            }
        }
        return token!!
    }

    private suspend fun fetchToken(): String {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AmadeusTokenService::class.java)

        return service.getToken(
            apiKey = API_KEY,
            apiSecret = API_SECRET
        ).access_token
    }
}
