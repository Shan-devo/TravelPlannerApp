package com.example.travelplannerapp.utilities

import android.os.Handler
import android.os.Looper
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapHelper {
    companion object {
        fun preload(map: MapView,  center: GeoPoint,
                    minZoom: Int = 12,
                    maxZoom: Int = 17) {
            val provider = map.tileProvider
            val handler = Handler(Looper.getMainLooper())

            for (zoom in minZoom..maxZoom) {
                map.controller.setZoom(zoom.toDouble())
                map.controller.setCenter(center)

                handler.postDelayed({
                    map.invalidate()
                }, 300)
            }
        }
    }
}