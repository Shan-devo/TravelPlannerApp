package com.example.travelplannerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.network.model.Hotel

class HotelAdapter(
    private var hotels: List<Hotel>,
    private val onClick: (Hotel) -> Unit
) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotels[position]
        holder.bind(hotel)
        holder.itemView.setOnClickListener {
            onClick(hotel)
        }
    }

    override fun getItemCount(): Int = hotels.size

    // 🔥 THIS WAS MISSING
    fun updateList(newHotels: List<Hotel>) {
        hotels = newHotels
        notifyDataSetChanged()
    }

    class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtHotelName)
        private val txtCity: TextView = itemView.findViewById(R.id.txtHotelCity)

        fun bind(hotel: Hotel) {
            txtName.text = hotel.name
            txtCity.text = hotel.address.cityName ?: "Unknown City"
        }
    }
}
