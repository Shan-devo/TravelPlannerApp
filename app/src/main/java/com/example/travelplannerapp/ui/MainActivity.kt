package com.example.travelplannerapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.travelplannerapp.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var locationOverlay: MyLocationNewOverlay? = null
    private var destinationMarker: Marker? = null

    companion object {
        private const val LOCATION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid configuration (MANDATORY)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setUseDataConnection(true) // ✅ stability

        // Default view (India)
        val india = GeoPoint(20.5937, 78.9629)
        map.controller.setZoom(5.0)
        map.controller.setCenter(india)

        checkLocationPermission()

        // SAFE map tap handling
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                addDestinationMarker(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        map.overlays.add(MapEventsOverlay(this, mapEventsReceiver))
    }

    private fun addDestinationMarker(point: GeoPoint) {
        destinationMarker?.let { map.overlays.remove(it) }

        destinationMarker = Marker(map).apply {
            position = point
            title = "Selected Destination"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        map.overlays.add(destinationMarker)
        map.controller.animateTo(point)
        map.invalidate()
    }

    private fun setupUserLocation() {
        if (locationOverlay != null) return

        locationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(this),
            map
        ).apply {
            enableMyLocation()
            // ❌ do NOT enableFollowLocation → causes crashes
        }

        map.overlays.add(locationOverlay)
    }

    private fun checkLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            setupUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
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
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            setupUserLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
        }
    }

    override fun onPause() {
        if (::map.isInitialized) {
            map.onPause()
        }
        super.onPause()
    }
}
