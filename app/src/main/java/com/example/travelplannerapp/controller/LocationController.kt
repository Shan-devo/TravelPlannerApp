package com.example.travelplannerapp.controller

import android.content.Context
import android.location.Location
import com.example.travelplannerapp.utilities.Utility.drawableToBitmap
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class LocationController(
    private val map: MapView,
    private val onLocationUpdate: (GeoPoint) -> Unit,
    private val icon: android.graphics.Bitmap
) {

    var currentLocation: GeoPoint? = null
        private set

    private var overlay: MyLocationNewOverlay? = null

    fun start(context: Context) {
        if (overlay != null) return

        val provider = object : GpsMyLocationProvider(context) {
            override fun onLocationChanged(location: Location) {
                super.onLocationChanged(location)

                val geo = GeoPoint(location.latitude, location.longitude)
                currentLocation = geo

                // 🔹 Notify UI
                onLocationUpdate(geo)
            }
        }
        overlay = MyLocationNewOverlay(provider, map).apply {
            enableMyLocation()

            // ✅ THIS is what actually shows
            setDirectionIcon(icon)

            // Optional (does not hurt)
            setPersonIcon(icon)

            // Accuracy circle (recommended)
            isDrawAccuracyEnabled = true

            runOnFirstFix {
                myLocation?.let { geo ->
                    currentLocation = geo
                    map.post {
                        map.controller.animateTo(geo)
                        map.controller.setZoom(16.0)
                        onLocationUpdate(geo)
                    }
                }
            }
        }


        map.overlays.add(overlay)
        map.invalidate()
    }

    fun stop() {
        overlay?.disableMyLocation()
        overlay = null
    }
}
