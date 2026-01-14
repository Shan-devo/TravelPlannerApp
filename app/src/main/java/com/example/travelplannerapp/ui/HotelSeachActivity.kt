package com.example.travelplannerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.adapter.HotelAdapter
import com.example.travelplannerapp.network.model.Address
import com.example.travelplannerapp.network.model.GeoCode
import com.example.travelplannerapp.network.model.Hotel

class HotelSearchActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var searchBox: EditText

    // ✅ FULLY VALID MOCK DATA
    private val allHotels = listOf(
        Hotel(
            hotelId = "H001",
            name = "Hotel Taj",
            geoCode = GeoCode(18.921984, 72.833245),
            address = Address("Mumbai")
        ),
        Hotel(
            hotelId = "H002",
            name = "The Oberoi",
            geoCode = GeoCode(28.613939, 77.209023),
            address = Address("Delhi")
        ),
        Hotel(
            hotelId = "H003",
            name = "ITC Grand",
            geoCode = GeoCode(12.971599, 77.594566),
            address = Address("Bangalore")
        ),
        Hotel(
            hotelId = "H004",
            name = "Leela Palace",
            geoCode = GeoCode(13.082680, 80.270718),
            address = Address("Chennai")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_hotel_search)

        searchBox = findViewById(R.id.etHotelSearch)
        recycler = findViewById(R.id.recyclerHotels)

        recycler.layoutManager = LinearLayoutManager(this)

        setAdapter(allHotels)

        searchBox.addTextChangedListener { editable ->
            val query = editable.toString().trim().lowercase()

            val filtered = if (query.isEmpty()) {
                allHotels
            } else {
                allHotels.filter { hotel ->
                    hotel.name.lowercase().contains(query) ||
                            hotel.address.cityName
                                ?.lowercase()
                                ?.contains(query) == true
                }
            }

            setAdapter(filtered)
        }
    }

    private fun setAdapter(list: List<Hotel>) {
        recycler.adapter = HotelAdapter(list) { hotel ->
            openHotelDetails(hotel)
        }
    }

    private fun openHotelDetails(hotel: Hotel) {
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
