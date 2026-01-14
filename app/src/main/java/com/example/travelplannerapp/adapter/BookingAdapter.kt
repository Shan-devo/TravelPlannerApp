package com.example.travelplannerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.network.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private val bookings: List<Booking>
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = bookings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtHotel = itemView.findViewById<TextView>(R.id.txtHotel)
        private val txtCity = itemView.findViewById<TextView>(R.id.txtCity)
        private val txtDates = itemView.findViewById<TextView>(R.id.txtDates)
        private val txtBookedAt = itemView.findViewById<TextView>(R.id.txtBookedAt)

        fun bind(b: Booking) {
            txtHotel.text = b.hotelName
            txtCity.text = b.city
            txtDates.text = "📅 ${b.checkInDate} → ${b.checkOutDate}"

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            txtBookedAt.text = "Booked on: ${sdf.format(Date(b.bookedAt))}"
        }
    }
}
