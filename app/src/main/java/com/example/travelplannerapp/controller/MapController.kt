package com.example.travelplannerapp.controller

import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import java.net.URL

class MapController(private val map: MapView) {

    private var destinationMarker: Marker? = null
    private var routeLine: Polyline? = null

    fun setup(onMapTap: (GeoPoint) -> Unit) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        // 🔴 MUST pass context
        map.overlays.add(
            MapEventsOverlay(
                map.context,
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onMapTap(p)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint) = false
                }
            )
        )
    }

    fun setDestination(point: GeoPoint, label: String) {
        destinationMarker?.let { map.overlays.remove(it) }

        destinationMarker = Marker(map).apply {
            position = point
            title = label
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        map.overlays.add(destinationMarker)
        map.controller.animateTo(point)
        map.invalidate()
    }

    fun drawRoute(start: GeoPoint, end: GeoPoint) {
        Thread {
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

                map.post {
                    routeLine?.let { map.overlays.remove(it) }

                    routeLine = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.BLUE
                        outlinePaint.strokeWidth = 8f
                        setPoints(points)
                    }

                    map.overlays.add(routeLine)
                    map.invalidate()
                }

            } catch (_: Exception) {}
        }.start()
    }

    fun getDestination(): GeoPoint? {
        return destinationMarker?.position
    }

}
