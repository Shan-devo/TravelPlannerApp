package com.example.travelplannerapp.controller

import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

class WeatherController {

    suspend fun getWeather(point: GeoPoint): String {
        return withContext(Dispatchers.IO) {
            val url =
                "https://api.met.no/weatherapi/locationforecast/2.0/compact" +
                        "?lat=${point.latitude}&lon=${point.longitude}"

            val connection = URL(url).openConnection()
            connection.setRequestProperty(
                "User-Agent",
                "TravelPlannerApp/1.0 github.com/yourname"
            )

            val response = connection.getInputStream()
                .bufferedReader()
                .use { it.readText() }

            val json = JSONObject(response)

            val details =
                json.getJSONObject("properties")
                    .getJSONArray("timeseries")
                    .getJSONObject(0)
                    .getJSONObject("data")
                    .getJSONObject("instant")
                    .getJSONObject("details")

            val temp = details.getDouble("air_temperature")

            "🌤 $temp°C"
        }
    }
}
