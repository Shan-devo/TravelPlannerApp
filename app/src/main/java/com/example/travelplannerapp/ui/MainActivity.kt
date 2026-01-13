package com.example.travelplannerapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.travelplannerapp.R
import com.example.travelplannerapp.controller.*
import com.example.travelplannerapp.data.FavoriteRoute
import com.example.travelplannerapp.utilities.FavoritesDbHelper
import com.example.travelplannerapp.utilities.Utility
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    /* ---------------- DRAWER ---------------- */
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    /* ---------------- CONTROLLERS ---------------- */
    private lateinit var mapController: MapController
    private lateinit var locationController: LocationController
    private lateinit var searchController: SearchController
    private lateinit var favoritesDb: FavoritesDbHelper

    /* ---------------- UI ---------------- */
    private lateinit var etCurrentLocation: AutoCompleteTextView
    private lateinit var etDestination: AutoCompleteTextView
    private lateinit var weatherLayout: LinearLayout
    private lateinit var txtWeather: TextView
    private lateinit var imgWeather: ImageView

    private lateinit var bottomRouteInfo: LinearLayout
    private lateinit var txtCar: TextView
    private lateinit var txtWalk: TextView
    private lateinit var txtBike: TextView
    private lateinit var btnRoute: Button
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button

    /* ---------------- ROUTE STATE ---------------- */
    private var lastDistanceKm = 0.0
    private var lastDurationMin = 0
    private var isRouteReady = false

    private val weatherController = WeatherController()

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* ---------------- OSMDROID CONFIG ---------------- */
        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            tileFileSystemCacheMaxBytes = 300L * 1024 * 1024
            tileFileSystemCacheTrimBytes = 250L * 1024 * 1024
        }

        setContentView(R.layout.activity_main)

        /* ---------------- TOOLBAR ---------------- */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setContentInsetsAbsolute(0, 0)

        /* ---------------- DRAWER ---------------- */
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable.color =
            ContextCompat.getColor(this, android.R.color.white)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else finish()
        }

        /* ---------------- VIEWS ---------------- */
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

        btnRoute = findViewById<Button>(R.id.btnRoute)
        btnClear = findViewById<Button>(R.id.btnClearRoute)
        btnSave = findViewById<Button>(R.id.btnSave)

        /* ===== ADDED: INITIAL BUTTON STATE ===== */
        btnRoute.isEnabled = false
        btnSave.visibility = View.GONE
        btnClear.visibility = View.GONE

        /* ---------------- CONTROLLERS ---------------- */
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

        /* ---------------- SEARCH ---------------- */
        searchController.setup { point, label ->
            mapController.setDestination(point, label)
            etDestination.setText(label)

            /* ===== ADDED: BUTTON RESET ON DESTINATION CHANGE ===== */
            isRouteReady = false
            updateRouteUI()

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

        /* ---------------- ROUTE ---------------- */
        btnRoute.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null) {
                Toast.makeText(this, "Waiting for GPS or destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isRouteReady = false
            mapController.drawRoute(start, end, {}, {})

            mapController.fetchRouteInfo(start, end) { routes ->
                runOnUiThread {
                    for (r in routes) {
                        val d = Utility.formatDuration(r.durationMin)
                        val km = "%.1f km".format(r.distanceKm)

                        when (r.profile) {
                            "driving" -> {
                                txtCar.text = "$d • $km"
                                lastDistanceKm = r.distanceKm
                                lastDurationMin = r.durationMin
                            }
                            "foot" -> txtWalk.text = "$d • $km"
                            "bike" -> txtBike.text = "$d • $km"
                        }
                    }

                    if (routes.isNotEmpty()) {
                        isRouteReady = true
                        updateRouteUI()

                        // 🔥 FORCE UI REFRESH
                        bottomRouteInfo.requestLayout()
                        bottomRouteInfo.invalidate()

                        btnSave.requestLayout()
                        btnClear.requestLayout()
                    }
                }
            }

            updateRouteUI()
        }

        /* ---------------- RESTORE ROUTE FROM FAVORITES ---------------- */
        intent?.let { i ->
            if (i.hasExtra("start_lat") && i.hasExtra("end_lat")) {

                val start = org.osmdroid.util.GeoPoint(
                    i.getDoubleExtra("start_lat", 0.0),
                    i.getDoubleExtra("start_lng", 0.0)
                )

                val end = org.osmdroid.util.GeoPoint(
                    i.getDoubleExtra("end_lat", 0.0),
                    i.getDoubleExtra("end_lng", 0.0)
                )

                val destinationName = i.getStringExtra("destination") ?: ""

                mapController.setDestination(end, destinationName)
                etDestination.setText(destinationName)

                mapController.drawRoute(start, end, {}, {})

                mapController.fetchRouteInfo(start, end) { routes ->
                    for (r in routes) {
                        val d = Utility.formatDuration(r.durationMin)
                        val km = "%.1f km".format(r.distanceKm)

                        when (r.profile) {
                            "driving" -> {
                                txtCar.text = "$d • $km"
                                lastDistanceKm = r.distanceKm
                                lastDurationMin = r.durationMin
                                isRouteReady = true

                                updateRouteUI()
                            }
                            "foot" -> txtWalk.text = "$d • $km"
                            "bike" -> txtBike.text = "$d • $km"
                        }
                    }
                    bottomRouteInfo.visibility = LinearLayout.VISIBLE
                }
            }
        }

        /* ---------------- CLEAR ---------------- */
        btnClear.setOnClickListener {
            mapController.clearRoute()
            bottomRouteInfo.visibility = LinearLayout.GONE
            weatherLayout.visibility = LinearLayout.GONE

            isRouteReady = false
            updateRouteUI()
            lastDistanceKm = 0.0
            lastDurationMin = 0
        }

        /* ---------------- SAVE ---------------- */
        btnSave.setOnClickListener {
            val start = locationController.currentLocation
            val end = mapController.getDestination()

            if (start == null || end == null || !isRouteReady) {
                Toast.makeText(this, "Please calculate route first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            favoritesDb.insert(
                FavoriteRoute(
                    startLat = start.latitude,
                    startLng = start.longitude,
                    endLat = end.latitude,
                    endLng = end.longitude,
                    destinationName = etDestination.text.toString().trim(),
                    distanceKm = lastDistanceKm,
                    durationMin = lastDurationMin
                )
            )

            Toast.makeText(this, "⭐ Route saved", Toast.LENGTH_SHORT).show()
        }

        val btnReverse = findViewById<ImageButton>(R.id.btnReverse)

        btnReverse.setOnClickListener {
            btnReverse.animate().rotationBy(180f).setDuration(250).start()
            val currentText = etCurrentLocation.text.toString()
            val destinationText = etDestination.text.toString()

            if (destinationText.isBlank()) {
                Toast.makeText(this, "No destination to reverse", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Swap text
            etDestination.setText(currentText)

            // Clear route safely
            mapController.clearRoute()
            bottomRouteInfo.visibility = View.GONE
            weatherLayout.visibility = View.GONE

            isRouteReady = false
            lastDistanceKm = 0.0
            lastDurationMin = 0

            updateRouteUI()

            Toast.makeText(this, "Locations reversed", Toast.LENGTH_SHORT).show()
        }

    }

    /* ---------------- DRAWER CLICK FIX ---------------- */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }

    /* ---------------- LIFECYCLE ---------------- */
    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    override fun onPause() {
        locationController.stop()
        super.onPause()
    }

    /* ---------------- PERMISSION ---------------- */
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

    private fun updateRouteUI() {
        if (isRouteReady) {
            bottomRouteInfo.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            btnClear.visibility = View.VISIBLE
            btnRoute.isEnabled = false
        } else {
            bottomRouteInfo.visibility = View.GONE
            btnSave.visibility = View.GONE
            btnClear.visibility = View.GONE
            btnRoute.isEnabled = etDestination.text.isNotBlank()
        }
    }

}
