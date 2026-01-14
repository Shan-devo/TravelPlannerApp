package com.example.travelplannerapp.network.model

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)

data class UnsplashPhoto(
    val urls: UnsplashUrls
)

data class UnsplashUrls(
    val regular: String
)
