package com.example.travelplannerapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.travelplannerapp.R
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.URL
import java.net.URLEncoder
import android.view.inputmethod.InputMethodManager;

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView

    private var locationOverlay: MyLocationNewOverlay? = null
    private var currentLocation: GeoPoint? = null

    private var destinationMarker: Marker? = null
    private var routeLine: Polyline? = null

    private lateinit var txtCurrentLocation: TextView
    private lateinit var txtDestination: TextView
    private lateinit var searchBox: AutoCompleteTextView
    private lateinit var searchAdapter: ArrayAdapter<String>

    private val searchResults = mutableListOf<Pair<String, GeoPoint>>()
    private var debounceJob: Job? = null

    companion object {
        private const val LOCATION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        txtCurrentLocation = findViewById(R.id.txtCurrentLocation)
        txtDestination = findViewById(R.id.txtDestination)
        searchBox = findViewById(R.id.searchLocation)

        setupMap()
        setupSearch()
        setupRouteButton()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        checkLocationPermission()
    }

    override fun onPause() {
        locationOverlay?.disableMyLocation()
        map.onPause()
        super.onPause()
    }

    /* ================= MAP ================= */

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        map.overlays.add(
            MapEventsOverlay(this, object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    setDestination(
                        p,
                        "Lat: %.5f, Lon: %.5f".format(p.latitude, p.longitude)
                    )
                    return true
                }
                override fun longPressHelper(p: GeoPoint) = false
            })
        )
    }

    /* ================= LOCATION ================= */

    private fun setupUserLocation() {
        if (locationOverlay != null) return

        val provider = object : GpsMyLocationProvider(this) {
            override fun onLocationChanged(location: Location) {
                super.onLocationChanged(location)
                currentLocation = GeoPoint(location.latitude, location.longitude)

                runOnUiThread {
                    txtCurrentLocation.text =
                        "📍 %.5f, %.5f".format(location.latitude, location.longitude)
                }
            }
        }

        val icon = drawableToBitmap(R.drawable.ic_my_location)

        locationOverlay = MyLocationNewOverlay(provider, map).apply {
            enableMyLocation()
            setPersonIcon(icon)

            runOnFirstFix {
                myLocation?.let {
                    currentLocation = it
                    runOnUiThread { map.controller.animateTo(it) }
                }
            }
        }

        map.overlays.add(locationOverlay)
        map.invalidate()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    /* ================= SEARCH ================= */

    private fun setupSearch() {
        searchAdapter = ArrayAdapter(
            this,
            R.layout.item_search_suggestion,
            R.id.txtTitle,
            mutableListOf()
        )


        searchBox.setAdapter(searchAdapter)
        searchBox.threshold = 1

        searchBox.doAfterTextChanged { text ->
            debounceJob?.cancel()
            val query = text.toString().trim()
            if (query.length < 2) return@doAfterTextChanged

            debounceJob = lifecycleScope.launch {
                delay(350)
                fetchSuggestions(query)
            }
        }

        searchBox.setOnItemClickListener { _, _, position, _ ->
            val (name, point) = searchResults[position]
            setDestination(point, name)
        }

        searchBox.setOnEditorActionListener { v, actionId, event ->
            val isEnter =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event?.keyCode == KeyEvent.KEYCODE_ENTER)

            if (isEnter) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    resolveSearchAndSetDestination(query)
                }
                true
            } else false
        }
    }

    private fun fetchSuggestions(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urlString =
                    "https://nominatim.openstreetmap.org/search" +
                            "?q=${URLEncoder.encode(query, "UTF-8")}" +
                            "&format=json&limit=5"

                val connection = URL(urlString).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000

                // 🔴 THIS IS MANDATORY
                connection.setRequestProperty(
                    "User-Agent",
                    "TravelPlannerApp/1.0 (your@email.com)"
                )

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val array = JSONArray(response)

                val names = mutableListOf<String>()
                searchResults.clear()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.getString("display_name")
                    val lat = obj.getDouble("lat")
                    val lon = obj.getDouble("lon")

                    names.add(name)
                    searchResults.add(name to GeoPoint(lat, lon))
                }

                withContext(Dispatchers.Main) {
                    searchAdapter.clear()
                    searchAdapter.addAll(names)
                    searchAdapter.notifyDataSetChanged()

                    if (names.isNotEmpty()) {
                        searchBox.requestFocus()          // 🔴 REQUIRED
                        searchBox.post {
                            searchBox.showDropDown()      // 🔴 REQUIRED
                        }
                    }
                }


            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Search error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun resolveSearchAndSetDestination(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/search?q=" +
                            URLEncoder.encode(query, "UTF-8") +
                            "&format=json&limit=1"

                val response = URL(url).readText()
                val arr = JSONArray(response)

                if (arr.length() == 0) {
                    withContext(Dispatchers.Main) {
                        toast("Location not found")
                    }
                    return@launch
                }

                val obj = arr.getJSONObject(0)
                val name = obj.getString("display_name")
                val point = GeoPoint(obj.getDouble("lat"), obj.getDouble("lon"))

                withContext(Dispatchers.Main) {
                    setDestination(point, name)
                }

            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Search failed")
                }
            }
        }
    }

    /* ================= DESTINATION ================= */

    private fun setDestination(point: GeoPoint, label: String) {
        destinationMarker?.let { map.overlays.remove(it) }

        destinationMarker = Marker(map).apply {
            position = point
            title = label
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        map.overlays.add(destinationMarker)
        map.controller.animateTo(point)
        hideKeyboard();
        searchAdapter.clear()
        searchAdapter.notifyDataSetChanged()
        searchResults.clear()
        map.invalidate()

        txtDestination.text = "📌 $label"
        searchBox.setText("")
        searchBox.clearFocus()
    }

    /* ================= ROUTE ================= */

    private fun setupRouteButton() {
        findViewById<Button>(R.id.btnRoute).setOnClickListener {
            val start = currentLocation
            val end = destinationMarker?.position

            if (start == null || end == null) {
                toast("Waiting for GPS or destination")
            } else {
                drawRoute(start, end)
            }
        }
    }

    private fun drawRoute(start: GeoPoint, end: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://router.project-osrm.org/route/v1/driving/" +
                            "${start.longitude},${start.latitude};" +
                            "${end.longitude},${end.latitude}" +
                            "?overview=full&geometries=geojson"

                val json = JSONObject(URL(url).readText())
                val coords =
                    json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                val points = ArrayList<GeoPoint>()
                for (i in 0 until coords.length()) {
                    val c = coords.getJSONArray(i)
                    points.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                }

                withContext(Dispatchers.Main) {
                    routeLine?.let { map.overlays.remove(it) }
                    routeLine = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.BLUE
                        outlinePaint.strokeWidth = 8f
                        setPoints(points)
                    }
                    map.overlays.add(routeLine)
                    map.invalidate()
                }

            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Route failed")
                }
            }
        }
    }

    /* ================= UTILS ================= */

    private fun drawableToBitmap(id: Int): android.graphics.Bitmap {
        val d = resources.getDrawable(id, null)
        val b = android.graphics.Bitmap.createBitmap(
            d.intrinsicWidth,
            d.intrinsicHeight,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val c = android.graphics.Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        return b
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchBox.windowToken, 0)
    }

}
