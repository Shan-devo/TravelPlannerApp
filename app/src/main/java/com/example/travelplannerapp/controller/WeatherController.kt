package com.example.travelplannerapp.controller

import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

class WeatherController {

    suspend fun getWeather(point: GeoPoint): WeatherResult {
        return withContext(Dispatchers.IO) {

            val url =
                "https://api.met.no/weatherapi/locationforecast/2.0/compact" +
                        "?lat=${point.latitude}&lon=${point.longitude}"

            val conn = URL(url).openConnection()
            conn.setRequestProperty(
                "User-Agent",
                "TravelPlannerApp/1.0 github.com/yourname"
            )

            val response = conn.inputStream
                .bufferedReader()
                .use { it.readText() }

            val json = JSONObject(response)

            val timeSeries =
                json.getJSONObject("properties")
                    .getJSONArray("timeseries")
                    .getJSONObject(0)
                    .getJSONObject("data")

            val temp =
                timeSeries
                    .getJSONObject("instant")
                    .getJSONObject("details")
                    .getDouble("air_temperature")

            val symbol =
                timeSeries
                    .getJSONObject("next_1_hours")
                    .getJSONObject("summary")
                    .getString("symbol_code")

            WeatherResult(temp, symbol)
        }
    }
}

data class WeatherResult(
    val temperature: Double,
    val symbolCode: String
)

