package com.example.travelplannerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.adapter.BookingAdapter
import com.example.travelplannerapp.repository.BookingRepository

class MyBookingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitvity_my_bookings)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "My Bookings"
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerBookings)
        recycler.layoutManager = LinearLayoutManager(this)

        val repository = BookingRepository(this)
        val bookings = repository.getMyBookings()

        recycler.adapter = BookingAdapter(bookings)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
