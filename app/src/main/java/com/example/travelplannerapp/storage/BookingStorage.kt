package com.example.travelplannerapp.storage

import android.content.Context
import com.example.travelplannerapp.network.model.Booking
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class BookingStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("booking_storage", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val KEY = "bookings"

    fun addBooking(
        hotelName: String,
        city: String,
        checkIn: String,
        checkOut: String
    ) {
        val bookings = getBookings().toMutableList()

        bookings.add(
            Booking(
                id = UUID.randomUUID().toString(),
                hotelName = hotelName,
                city = city,
                checkInDate = checkIn,
                checkOutDate = checkOut
            )
        )

        save(bookings)
    }

    fun getBookings(): List<Booking> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Booking>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteBooking(id: String) {
        val updated = getBookings().filterNot { it.id == id }
        save(updated)
    }

    fun clearAll() {
        prefs.edit().remove(KEY).apply()
    }

    private fun save(list: List<Booking>) {
        prefs.edit()
            .putString(KEY, gson.toJson(list))
            .apply()
    }
}
