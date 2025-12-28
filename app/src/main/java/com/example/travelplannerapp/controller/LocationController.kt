package com.example.travelplannerapp.controller

import android.content.Context
import android.location.Location
import android.widget.TextView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class LocationController(
    private val map: MapView,
    private val txtLocation: TextView,
    private val iconBitmap: android.graphics.Bitmap
) {

    var currentLocation: GeoPoint? = null
        private set

    private var overlay: MyLocationNewOverlay? = null

    fun start(context: Context) {
        if (overlay != null) return

        val provider = object : GpsMyLocationProvider(context) {
            override fun onLocationChanged(location: Location) {
                super.onLocationChanged(location)
                currentLocation = GeoPoint(location.latitude, location.longitude)

                txtLocation.post {
                    txtLocation.text =
                        "📍 %.5f, %.5f"
                            .format(location.latitude, location.longitude)
                }
            }
        }

        overlay = MyLocationNewOverlay(provider, map).apply {
            enableMyLocation()
            setPersonIcon(iconBitmap)

            runOnFirstFix {
                myLocation?.let {
                    currentLocation = it
                    map.post {
                        map.controller.animateTo(it)
                        map.controller.setZoom(16.0)
                    }
                }
            }
        }

        map.overlays.add(overlay)
        map.invalidate()
    }

    fun stop() {
        overlay?.disableMyLocation()
    }
}
