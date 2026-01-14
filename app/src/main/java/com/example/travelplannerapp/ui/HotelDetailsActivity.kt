package com.example.travelplannerapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.travelplannerapp.R
import com.example.travelplannerapp.repository.BookingRepository
import java.util.*

class HotelDetailsActivity : AppCompatActivity() {

    private lateinit var imgHotel: ImageView
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

        /* ---------- TOOLBAR ---------- */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookingRepository = BookingRepository(this)

        /* ---------- VIEWS ---------- */
        imgHotel = findViewById(R.id.imgHotel)
        txtHotelName = findViewById(R.id.txtHotelName)
        txtHotelCity = findViewById(R.id.txtHotelCity)
        btnCheckIn = findViewById(R.id.btnCheckIn)
        btnCheckOut = findViewById(R.id.btnCheckOut)
        spinnerRoomType = findViewById(R.id.spinnerRoomType)
        btnProceed = findViewById(R.id.btnProceed)

        val hotelName = intent.getStringExtra("hotelName") ?: "Hotel"
        val hotelCity = intent.getStringExtra("hotelCity") ?: "City"

        txtHotelName.text = hotelName
        txtHotelCity.text = hotelCity

        /* ---------- LOAD HOTEL IMAGE (UNSPLASH) ---------- */
        loadHotelImage(hotelName, hotelCity)

        setupRoomTypes()
        setupDatePickers()

        btnProceed.setOnClickListener {
            saveBooking()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /* ---------- UNSPLASH IMAGE ---------- */
    private fun loadHotelImage(name: String, city: String) {
        val query = "${name} hotel $city".replace(" ", "%20")

        val imageUrl =
            "https://source.unsplash.com/800x600/?$query"

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_hotel_placeholder) // optional
            .error(R.drawable.ic_hotel_placeholder)
            .into(imgHotel)
    }

    /* ---------- ROOM TYPES ---------- */
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

    /* ---------- DATE PICKERS ---------- */
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

    /* ---------- SAVE BOOKING ---------- */
    private fun saveBooking() {
        if (checkInDate == null || checkOutDate == null) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }

        bookingRepository.bookHotel(
            hotelName = txtHotelName.text.toString(),
            city = txtHotelCity.text.toString(),
            checkIn = checkInDate!!,
            checkOut = checkOutDate!!
        )

        Toast.makeText(this, "✅ Booking saved successfully", Toast.LENGTH_LONG).show()
        finish()
    }


    /* ---------- DATE PICKER ---------- */
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
