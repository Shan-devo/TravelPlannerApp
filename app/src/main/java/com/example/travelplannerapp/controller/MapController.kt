package com.example.travelplannerapp.controller

import com.example.travelplannerapp.data.RouteInfo
import com.example.travelplannerapp.utilities.MapHelper
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
    private var isRouteActive = false

    fun setup(onMapTap: (GeoPoint) -> Unit) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        map.overlays.add(
            MapEventsOverlay(
                map.context,
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (!isRouteActive) onMapTap(p)
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint) = false
                }
            )
        )
    }

    fun setDestination(point: GeoPoint, label: String) {
        if (isRouteActive) return

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

    fun drawRoute(
        start: GeoPoint,
        end: GeoPoint,
        onRouteReady: (RouteInfo) -> Unit,
        onError: () -> Unit
    ) {
        Thread {
            try {
                val url =
                    "https://router.project-osrm.org/route/v1/driving/" +
                            "${start.longitude},${start.latitude};" +
                            "${end.longitude},${end.latitude}" +
                            "?overview=full&geometries=geojson"

                val json = JSONObject(URL(url).readText())
                val route = json.getJSONArray("routes").getJSONObject(0)

                val distance = route.getDouble("distance")
                val duration = route.getDouble("duration")

                val coords = route
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
                    isRouteActive = true

                    onRouteReady(
                        RouteInfo(
                            profile = "driving",
                            distanceKm = distance,
                            durationMin = duration.toInt()
                        )
                    )
                }

                MapHelper.preload(map,end);
            } catch (e: Exception) {
                map.post { onError() }
            }
        }.start()
    }

    fun fetchRouteInfo(
        start: GeoPoint,
        end: GeoPoint,
        onResult: (List<RouteInfo>) -> Unit
    ) {
        val profiles = listOf("driving", "foot", "bike")
        val results = mutableListOf<RouteInfo>()

        Thread {
            try {
                for (profile in profiles) {
                    val url =
                        "https://router.project-osrm.org/route/v1/$profile/" +
                                "${start.longitude},${start.latitude};" +
                                "${end.longitude},${end.latitude}" +
                                "?overview=false"

                    val json = JSONObject(URL(url).readText())
                    val route = json.getJSONArray("routes").getJSONObject(0)

                    results.add(
                        RouteInfo(
                            profile = profile,
                            durationMin = (route.getDouble("duration") / 60).toInt(),
                            distanceKm = route.getDouble("distance") / 1000.0
                        )
                    )
                }

                map.post { onResult(results) }

            } catch (_: Exception) {}
        }.start()
    }

    fun getDestination(): GeoPoint? = destinationMarker?.position

    fun isRouteShowing(): Boolean = isRouteActive

    fun clearRoute() {
        routeLine?.let { map.overlays.remove(it) }
        routeLine = null
        isRouteActive = false
        map.invalidate()
    }
}
