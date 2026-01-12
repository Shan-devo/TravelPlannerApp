package com.example.travelplannerapp.controller

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.*
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import java.net.URL
import java.net.URLEncoder

class SearchController(
    private val searchBox: AutoCompleteTextView,
    private val scope: LifecycleCoroutineScope
) {

    private val results = mutableListOf<Pair<String, GeoPoint>>()
    private var searchJob: Job? = null

    private val adapter = ArrayAdapter(
        searchBox.context,
        com.example.travelplannerapp.R.layout.item_search_suggestion,
        com.example.travelplannerapp.R.id.txtTitle,
        mutableListOf<String>()
    )

    fun setup(onResult: (GeoPoint, String) -> Unit) {
        searchBox.setAdapter(adapter)
        searchBox.threshold = 1

        searchBox.doAfterTextChanged {
            val query = it.toString().trim()
            if (query.length < 2) return@doAfterTextChanged

            searchJob?.cancel()
            searchJob = scope.launch {
                delay(350)
                fetch(query)
            }
        }

        searchBox.setOnItemClickListener { _, _, pos, _ ->
            val (label, point) = results[pos]
            searchBox.setText(label)   // ✅ KEEP full destination text
            onResult(point, label)
            clearSuggestions()
        }
    }

    private fun fetch(query: String) {
        results.clear()
        scope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/search?q=" +
                            URLEncoder.encode(query, "UTF-8") +
                            "&format=json&limit=5"

                val conn = URL(url).openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty(
                    "User-Agent",
                    "TravelPlannerApp/1.0"
                )

                val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                val names = mutableListOf<String>()

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.getString("display_name")
                    val point =
                        GeoPoint(obj.getDouble("lat"), obj.getDouble("lon"))

                    names.add(name)
                    results.add(name to point)
                }

                withContext(Dispatchers.Main) {
                    adapter.clear()
                    adapter.addAll(names)
                    adapter.notifyDataSetChanged()
                    if (names.isNotEmpty()) searchBox.showDropDown()
                }

            } catch (_: Exception) {}
        }
    }

    private fun clearSuggestions() {
        adapter.clear()
        results.clear()
        searchBox.clearFocus()
    }
}
