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

    // 🔹 New UI
    private lateinit var etCurrentLocation: AutoCompleteTextView
    private lateinit var etDestination: AutoCompleteTextView
    private lateinit var weatherLayout: LinearLayout
    private lateinit var txtWeather: TextView
    private lateinit var imgWeather: ImageView

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

        // ✅ OSMDroid + Offline cache
        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            tileFileSystemCacheMaxBytes = 300L * 1024 * 1024
            tileFileSystemCacheTrimBytes = 250L * 1024 * 1024
        }

        setContentView(R.layout.activity_main)

        /* -------------------- VIEWS -------------------- */

        val map = findViewById<MapView>(R.id.map)

        etCurrentLocation = findViewById(R.id.etCurrentLocation)
        etDestination = findViewById(R.id.etDestination)

        weatherLayout = findViewById(R.id.weatherLayout)
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
            onLocationUpdate = { geo ->
                etCurrentLocation.setText(
                    "%.5f, %.5f".format(geo.latitude, geo.longitude)
                )
            },
            icon = Utility.drawableToBitmap(this, R.drawable.ic_my_location)
        )

        searchController = SearchController(etDestination, lifecycleScope)
        favoritesDb = FavoritesDbHelper(this)

        /* -------------------- SEARCH DESTINATION -------------------- */

        searchController.setup { point, label ->
            mapController.setDestination(point, label)

            etDestination.setText(label)
            etDestination.clearFocus()
            Utility.hideKeyboard(this, etDestination.windowToken)

            lifecycleScope.launch {
                val weather = weatherController.getWeather(point)
                txtWeather.text = "${weather.temperature}°C"
                imgWeather.setImageResource(
                    Utility.weatherIcon(weather.symbolCode)
                )
                weatherLayout.visibility = LinearLayout.VISIBLE
            }
        }

        /* -------------------- ROUTE -------------------- */

        btnRoute.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "Waiting for GPS or destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mapController.drawRoute(start, end, {}, {
                Toast.makeText(this, "Route failed", Toast.LENGTH_SHORT).show()
            })

            mapController.fetchRouteInfo(start, end) { routes ->
                for (r in routes) {
                    val d = Utility.formatDuration(r.durationMin)
                    val km = "%.1f km".format(r.distanceKm)

                    when (r.profile) {
                        "driving" -> txtCar.text = "$d • $km"
                        "foot" -> txtWalk.text = "$d • $km"
                        "bike" -> txtBike.text = "$d • $km"
                    }
                }
                bottomRouteInfo.visibility = LinearLayout.VISIBLE
            }
        }

        /* -------------------- CLEAR -------------------- */

        btnClear.setOnClickListener {
            mapController.clearRoute()
            bottomRouteInfo.visibility = LinearLayout.GONE
            etDestination.setText("")
            weatherLayout.visibility = LinearLayout.GONE
        }

        /* -------------------- SAVE FAVORITE -------------------- */

        btnSave.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "No route to save", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            favoritesDb.insert(
                FavoriteRoute(
                    startLat = start.latitude,
                    startLng = start.longitude,
                    endLat = end.latitude,
                    endLng = end.longitude,
                    destinationName = etDestination.text.toString(),
                    distanceKm = 0.0,
                    durationMin = 0
                )
            )

            Toast.makeText(this, "⭐ Route saved", Toast.LENGTH_SHORT).show()
        }

        /* -------------------- FAVORITES -------------------- */

        findViewById<Button>(R.id.btnFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    override fun onPause() {
        locationController.stop()
        super.onPause()
    }

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
