package com.example.travelplannerapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelplannerapp.R
import com.example.travelplannerapp.controller.*
import com.example.travelplannerapp.utilities.Utility
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    private lateinit var mapController: MapController
    private lateinit var locationController: LocationController
    private lateinit var searchController: SearchController

    private lateinit var txtWeather: TextView
    private lateinit var imgWeather: ImageView

    private lateinit var bottomRouteInfo: LinearLayout
    private lateinit var txtCar: TextView
    private lateinit var txtWalk: TextView
    private lateinit var txtBike: TextView

    private val weatherController = WeatherController()

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        val map = findViewById<MapView>(R.id.map)
        val txtCurrent = findViewById<TextView>(R.id.txtCurrentLocation)
        val txtDest = findViewById<TextView>(R.id.txtDestination)
        val searchBox = findViewById<AutoCompleteTextView>(R.id.searchLocation)

        txtWeather = findViewById(R.id.txtWeather)
        imgWeather = findViewById(R.id.imgWeather)

        bottomRouteInfo = findViewById(R.id.bottomRouteInfo)
        bottomRouteInfo = findViewById(R.id.bottomRouteInfo)
        txtCar = findViewById(R.id.txtCar)
        txtWalk = findViewById(R.id.txtWalk)
        txtBike = findViewById(R.id.txtBike)


        mapController = MapController(map)

        locationController = LocationController(
            map,
            txtCurrent,
            Utility.drawableToBitmap(this, R.drawable.ic_my_location)
        )

        searchController = SearchController(searchBox, lifecycleScope)

        mapController.setup { point ->
            mapController.setDestination(point, "Selected location")
            txtDest.text = "📌 Selected location"
        }

        searchController.setup { point, label ->
            mapController.setDestination(point, label)
            txtDest.text = "📌 $label"

            Utility.hideKeyboard(this, searchBox.windowToken)

            lifecycleScope.launch {
                val weather = weatherController.getWeather(point)
                txtWeather.text = "${weather.temperature}°C"
                imgWeather.setImageResource(
                    Utility.weatherIcon(weather.symbolCode)
                )
            }
        }

        findViewById<Button>(R.id.btnRoute).setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "Waiting for GPS or destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mapController.drawRoute(
                start,
                end,
                onRouteReady = {
                    // route drawn (car)
                },
                onError = {
                    Toast.makeText(this, "Route failed", Toast.LENGTH_SHORT).show()
                }
            )

            // 🔥 Fetch ALL transport ETAs
            mapController.fetchRouteInfo(start, end) { routes ->

                for (route in routes) {
                    val durationText = Utility.formatDuration(route.durationMin.toInt())
                    val distanceText = "%.1f km".format(route.distanceKm)

                    when (route.profile) {
                        "driving" -> {
                            txtCar.text = "$durationText • $distanceText"
                        }
                        "foot" -> {
                            txtWalk.text = "$durationText • $distanceText"
                        }
                        "bike" -> {
                            txtBike.text = "$durationText • $distanceText"
                        }
                    }
                }

                bottomRouteInfo.visibility = LinearLayout.VISIBLE
            }


        }

        findViewById<Button>(R.id.btnClearRoute).setOnClickListener {
            mapController.clearRoute()
            bottomRouteInfo.visibility = LinearLayout.GONE
            txtDest.text = "Where to?"
            txtWeather.text = "🌤 Weather"
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
