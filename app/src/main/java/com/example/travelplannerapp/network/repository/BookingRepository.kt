package com.example.travelplannerapp.network.repository

import android.content.Context
import com.example.travelplannerapp.network.model.Booking
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class BookingRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences("bookings_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun bookHotel(
        hotelName: String,
        city: String,
        checkInDate: String,
        checkOutDate: String
    ) {
        val bookings = getBookings().toMutableList()

        bookings.add(
            Booking(
                id = UUID.randomUUID().toString(),
                hotelName = hotelName,
                city = city,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate
            )
        )

        prefs.edit()
            .putString("bookings", gson.toJson(bookings))
            .apply()
    }

    fun getBookings(): List<Booking> {
        val json = prefs.getString("bookings", null) ?: return emptyList()
        val type = object : TypeToken<List<Booking>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteBooking(id: String) {
        val updated = getBookings().filterNot { it.id == id }
        prefs.edit()
            .putString("bookings", gson.toJson(updated))
            .apply()
    }
}
