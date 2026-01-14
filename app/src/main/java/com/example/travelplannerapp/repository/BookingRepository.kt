package com.example.travelplannerapp.repository

import android.content.Context
import com.example.travelplannerapp.network.model.Booking
import com.example.travelplannerapp.storage.BookingStorage

class BookingRepository(context: Context) {

    private val storage = BookingStorage(context)

    fun bookHotel(
        hotelName: String,
        city: String,
        checkIn: String,
        checkOut: String
    ) {
        storage.addBooking(hotelName, city, checkIn, checkOut)
    }

    fun getMyBookings(): List<Booking> {
        return storage.getBookings()
    }

    fun cancelBooking(id: String) {
        storage.deleteBooking(id)
    }
}
