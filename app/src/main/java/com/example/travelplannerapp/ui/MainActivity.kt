package com.example.travelplannerapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelplannerapp.R
import com.example.travelplannerapp.controller.*
import com.example.travelplannerapp.data.FavoriteRoute
import com.example.travelplannerapp.utilities.FavoritesDbHelper
import com.example.travelplannerapp.utilities.Utility
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    private lateinit var mapController: MapController
    private lateinit var locationController: LocationController
    private lateinit var searchController: SearchController

    private lateinit var txtWeather: TextView
    private lateinit var imgWeather: ImageView
    private lateinit var txtDestination: TextView

    private lateinit var bottomRouteInfo: LinearLayout
    private lateinit var txtCar: TextView
    private lateinit var txtWalk: TextView
    private lateinit var txtBike: TextView

    private lateinit var btnSave: Button
    private lateinit var favoritesDb: FavoritesDbHelper

    private val weatherController = WeatherController()

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ OSMDroid config
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        /* -------------------- VIEWS -------------------- */

        val map = findViewById<MapView>(R.id.map)
        val txtCurrent = findViewById<TextView>(R.id.txtCurrentLocation)
        txtDestination = findViewById(R.id.txtDestination)
        val searchBox = findViewById<AutoCompleteTextView>(R.id.searchLocation)

        txtWeather = findViewById(R.id.txtWeather)
        imgWeather = findViewById(R.id.imgWeather)

        bottomRouteInfo = findViewById(R.id.bottomRouteInfo)
        txtCar = findViewById(R.id.txtCar)
        txtWalk = findViewById(R.id.txtWalk)
        txtBike = findViewById(R.id.txtBike)

        val btnRoute = findViewById<Button>(R.id.btnRoute)
        val btnClear = findViewById<Button>(R.id.btnClearRoute)
        btnSave = findViewById(R.id.btnSave)

        /* -------------------- CONTROLLERS -------------------- */

        mapController = MapController(map)

        locationController = LocationController(
            map,
            txtCurrent,
            Utility.drawableToBitmap(this, R.drawable.ic_my_location)
        )

        searchController = SearchController(searchBox, lifecycleScope)

        favoritesDb = FavoritesDbHelper(this)

        /* -------------------- MAP TAP -------------------- */

        mapController.setup { point ->
            mapController.setDestination(point, "Selected location")
            txtDestination.text = "📌 Selected location"
        }

        /* -------------------- SEARCH -------------------- */

        searchController.setup { point, label ->
            mapController.setDestination(point, label)
            txtDestination.text = "📌 $label"

            Utility.hideKeyboard(this, searchBox.windowToken)

            lifecycleScope.launch {
                val weather = weatherController.getWeather(point)
                txtWeather.text = "${weather.temperature}°C"
                imgWeather.setImageResource(
                    Utility.weatherIcon(weather.symbolCode)
                )
            }
        }

        /* -------------------- SHOW ROUTE -------------------- */

        btnRoute.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "Waiting for GPS or destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mapController.drawRoute(
                start,
                end,
                onRouteReady = {},
                onError = {
                    Toast.makeText(this, "Route failed", Toast.LENGTH_SHORT).show()
                }
            )

            mapController.fetchRouteInfo(start, end) { routes ->
                for (route in routes) {
                    val duration = Utility.formatDuration(route.durationMin)
                    val distance = "%.1f km".format(route.distanceKm)

                    when (route.profile) {
                        "driving" -> txtCar.text = "$duration • $distance"
                        "foot" -> txtWalk.text = "$duration • $distance"
                        "bike" -> txtBike.text = "$duration • $distance"
                    }
                }
                bottomRouteInfo.visibility = LinearLayout.VISIBLE
            }
        }

        /* -------------------- CLEAR ROUTE -------------------- */

        btnClear.setOnClickListener {
            mapController.clearRoute()
            bottomRouteInfo.visibility = LinearLayout.GONE
            txtDestination.text = "Where to?"
            txtWeather.text = "🌤 Weather"
        }

        /* -------------------- SAVE FAVORITE -------------------- */

        val db = FavoritesDbHelper(this)

        btnSave.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "No route to save", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.insert(
                FavoriteRoute(
                    startLat = start.latitude,
                    startLng = start.longitude,
                    endLat = end.latitude,
                    endLng = end.longitude,
                    destinationName = txtDestination.text.toString(),
                    distanceKm = 5.4,      // replace later with real value
                    durationMin = 18       // replace later with real value
                )
            )

            Toast.makeText(this, "⭐ Route saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnFavorites).setOnClickListener {
            startActivity(
                Intent(this, FavoritesActivity::class.java)
            )
        }

        // 🔹 Handle route opened from Favorites
        intent?.let {
            if (it.hasExtra("start_lat")) {

                val start = GeoPoint(
                    it.getDoubleExtra("start_lat", 0.0),
                    it.getDoubleExtra("start_lng", 0.0)
                )

                val end = GeoPoint(
                    it.getDoubleExtra("end_lat", 0.0),
                    it.getDoubleExtra("end_lng", 0.0)
                )

                val destination = it.getStringExtra("destination") ?: "Favorite"

                txtDestination.text = "📌 $destination"

                mapController.setDestination(end, destination)

                map.post {
                    mapController.drawRoute(
                        start,
                        end,
                        onRouteReady = {},
                        onError = {
                            Toast.makeText(this, "Route failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

    }

    /* -------------------- LIFECYCLE -------------------- */

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    override fun onPause() {
        locationController.stop()
        super.onPause()
    }

    /* -------------------- PERMISSIONS -------------------- */

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationController.start(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }
}
