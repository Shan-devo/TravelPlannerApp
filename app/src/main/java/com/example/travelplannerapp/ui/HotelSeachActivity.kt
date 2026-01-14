package com.example.travelplannerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.adapter.HotelAdapter
import com.example.travelplannerapp.network.model.Hotel
import com.example.travelplannerapp.repository.HotelRepository
import kotlinx.coroutines.launch

class HotelSearchActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var searchBox: EditText
    private lateinit var adapter: HotelAdapter

    private val repository = HotelRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        searchBox = findViewById(R.id.etHotelSearch)
        recycler = findViewById(R.id.recyclerHotels)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = HotelAdapter(emptyList()) { hotel ->
            openHotelDetails(hotel)
        }
        recycler.adapter = adapter

        searchBox.addTextChangedListener { text ->
            val cityCode = text.toString().trim().uppercase()

            // ✅ Amadeus requires EXACTLY 3-letter city code
            if (cityCode.length != 3) return@addTextChangedListener

            lifecycleScope.launch {
                try {
                    val hotels = repository.searchHotels(cityCode)
                    adapter.updateList(hotels)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@HotelSearchActivity,
                        "Failed to load hotels",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openHotelDetails(hotel: Hotel) {
        val intent = Intent(this, HotelDetailsActivity::class.java).apply {
            putExtra("hotelName", hotel.name)
            putExtra("hotelCity", hotel.address.cityName ?: "")
        }
        startActivity(intent)
    }
}
