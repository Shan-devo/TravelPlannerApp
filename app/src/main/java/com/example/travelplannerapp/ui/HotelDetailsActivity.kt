package com.example.travelplannerapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.travelplannerapp.R
import com.example.travelplannerapp.repository.BookingRepository
import java.util.*

class HotelDetailsActivity : AppCompatActivity() {

    private lateinit var txtHotelName: TextView
    private lateinit var txtHotelCity: TextView
    private lateinit var btnCheckIn: Button
    private lateinit var btnCheckOut: Button
    private lateinit var spinnerRoomType: Spinner
    private lateinit var btnProceed: Button

    private var checkInDate: String? = null
    private var checkOutDate: String? = null

    private lateinit var bookingRepository: BookingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_details)

        bookingRepository = BookingRepository(this)

        txtHotelName = findViewById(R.id.txtHotelName)
        txtHotelCity = findViewById(R.id.txtHotelCity)
        btnCheckIn = findViewById(R.id.btnCheckIn)
        btnCheckOut = findViewById(R.id.btnCheckOut)
        spinnerRoomType = findViewById(R.id.spinnerRoomType)
        btnProceed = findViewById(R.id.btnProceed)

        txtHotelName.text = intent.getStringExtra("hotelName")
        txtHotelCity.text = intent.getStringExtra("hotelCity")

        setupRoomTypes()
        setupDatePickers()

        btnProceed.setOnClickListener {
            saveBooking()
        }
    }

    private fun setupRoomTypes() {
        val rooms = listOf(
            "Standard Room - ₹2,500/night",
            "Deluxe Room - ₹4,000/night",
            "Suite - ₹6,500/night"
        )

        spinnerRoomType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            rooms
        )
    }

    private fun setupDatePickers() {
        btnCheckIn.setOnClickListener {
            showDatePicker { date ->
                checkInDate = date
                btnCheckIn.text = "Check-in: $date"
            }
        }

        btnCheckOut.setOnClickListener {
            showDatePicker { date ->
                checkOutDate = date
                btnCheckOut.text = "Check-out: $date"
            }
        }
    }

    private fun saveBooking() {
        if (checkInDate == null || checkOutDate == null) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }

        val roomType = spinnerRoomType.selectedItem.toString()

        bookingRepository.bookHotel(
            hotelName = "${txtHotelName.text} ($roomType)",
            city = txtHotelCity.text.toString(),
            checkIn = checkInDate!!,
            checkOut = checkOutDate!!
        )

        Toast.makeText(this, "✅ Booking saved successfully", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showDatePicker(onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                onSelected("$day/${month + 1}/$year")
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
