package com.example.travelplannerapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.network.model.Hotel
import com.example.travelplannerapp.ui.HotelDetailsActivity

class HotelAdapter(
    private val hotels: List<Hotel>,
    private val onClick: (Hotel) -> Unit
) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(hotels[position])
    }

    override fun getItemCount() = hotels.size

    class HotelViewHolder(
        itemView: View,
        private val onClick: (Hotel) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtName: TextView = itemView.findViewById(R.id.txtHotelName)
        private val txtCity: TextView = itemView.findViewById(R.id.txtHotelCity)

        fun bind(hotel: Hotel) {
            txtName.text = hotel.name
            txtCity.text = hotel.address.cityName ?: "Unknown"

            itemView.setOnClickListener {
                onClick(hotel)
            }
        }
    }
}

