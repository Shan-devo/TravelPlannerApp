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
import com.example.travelplannerapp.controller.LocationController
import com.example.travelplannerapp.controller.MapController
import com.example.travelplannerapp.controller.SearchController
import com.example.travelplannerapp.controller.WeatherController
import com.example.travelplannerapp.utilities.Utility
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var mapController: MapController
    private lateinit var locationController: LocationController
    private lateinit var searchController: SearchController

    private lateinit var txtWeather: TextView

    private val weatherController = WeatherController()

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ OSMDroid config (MANDATORY)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        // ✅ Bind views AFTER setContentView
        map = findViewById(R.id.map)
        val txtCurrent = findViewById<TextView>(R.id.txtCurrentLocation)
        val txtDest = findViewById<TextView>(R.id.txtDestination)
        val searchBox = findViewById<AutoCompleteTextView>(R.id.searchLocation)

        // ✅ Controllers
        mapController = MapController(map)

        locationController = LocationController(
            map,
            txtCurrent,
            Utility.drawableToBitmap(this, R.drawable.ic_my_location)
        )

        searchController = SearchController(searchBox, lifecycleScope)

        // ✅ Map tap → destination
        mapController.setup { point ->
            mapController.setDestination(
                point,
                "Lat: %.5f, Lon: %.5f".format(point.latitude, point.longitude)
            )
            if(!mapController.isRouteShowing()) {
                txtDest.text = "📌 Selected location"
            }
        }


        // ✅ Search → destination
        searchController.setup { point, label ->
            mapController.setDestination(point, label)
            txtDest.text = "📌 $label"
            Utility.hideKeyboard(this, searchBox.windowToken)

            lifecycleScope.launch {
                txtWeather.text = weatherController.getWeather(point)
            }
        }


        val btnRoute = findViewById<Button>(R.id.btnRoute)
        btnRoute.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(
                    this,
                    "Waiting for GPS or destination",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                mapController.drawRoute(start, end)
            }
        }

        val btnClear = findViewById<Button>(R.id.btnClearRoute)

        btnClear.setOnClickListener {
            mapController.clearRoute()
            Toast.makeText(this, "Route cleared", Toast.LENGTH_SHORT).show()
            txtWeather.text = "🌤 Weather"
            txtDest.text = "Where to?"
        }

        txtWeather = findViewById(R.id.txtWeather)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()            // 🔴 REQUIRED
        checkLocationPermission()
    }

    override fun onPause() {
        locationController.stop()
        map.onPause()             // 🔴 REQUIRED
        super.onPause()
    }

    /* ================= PERMISSION ================= */

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            locationController.start(this)
        }
    }
}
