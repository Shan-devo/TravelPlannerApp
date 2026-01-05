package com.example.travelplannerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.data.FavoriteRoute

class FavoriteAdapter(
    private val list: MutableList<FavoriteRoute>,
    private val onClick: (FavoriteRoute) -> Unit,
    private val onDelete: (FavoriteRoute) -> Unit,
    private val onEdit: (FavoriteRoute, Int) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.txtTitle)
        val info: TextView = v.findViewById(R.id.txtInfo)
        val btnDelete: Button = v.findViewById(R.id.btnDelete)

        val btnEdit: Button = v.findViewById(R.id.btnEdit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_route, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val route = list[pos]

        h.title.text = route.destinationName
        h.info.text = "${route.durationMin} min • %.1f km".format(route.distanceKm)

        // ✅ TAP → OPEN MAP
        h.itemView.setOnClickListener {
            onClick(route)
        }

        // ✅ DELETE
        h.btnDelete.setOnClickListener {
            onDelete(route)
            list.removeAt(pos)
            notifyItemRemoved(pos)
        }

        h.btnEdit.setOnClickListener {
            onEdit(route, pos)
        }

    }

    override fun getItemCount() = list.size
}
