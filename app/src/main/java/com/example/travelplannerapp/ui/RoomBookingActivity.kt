package com.example.travelplannerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.adapter.HotelAdapter
import com.example.travelplannerapp.repository.HotelRepository
import kotlinx.coroutines.launch

class RoomBookingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val hotelRepository = HotelRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_booking)

        recyclerView = findViewById(R.id.recyclerHotels)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cityCode = intent.getStringExtra("cityCode") ?: "DEL"

        loadHotels(cityCode)
    }

    private fun loadHotels(cityCode: String) {
        lifecycleScope.launch {
            try {
                val hotels = hotelRepository.searchHotels(cityCode)

                recyclerView.adapter = HotelAdapter(hotels) { hotel ->
                    openHotelDetails(hotel)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@RoomBookingActivity,
                    "Failed to load hotels",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openHotelDetails(hotel: com.example.travelplannerapp.network.model.Hotel) {
        val intent = Intent(this, HotelDetailsActivity::class.java).apply {
            putExtra("hotelId", hotel.hotelId)
            putExtra("hotelName", hotel.name)
            putExtra("hotelCity", hotel.address.cityName ?: "Unknown")
            putExtra("lat", hotel.geoCode.latitude)
            putExtra("lng", hotel.geoCode.longitude)
        }
        startActivity(intent)
    }
}
