package com.example.travelplannerapp.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class TokenResponse(
    val access_token: String,
    val expires_in: Int
)

interface AmadeusTokenService {

    @FormUrlEncoded
    @POST("v1/security/oauth2/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") apiKey: String,
        @Field("client_secret") apiSecret: String
    ): TokenResponse
}
