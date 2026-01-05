package com.example.travelplannerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelplannerapp.R
import com.example.travelplannerapp.adapter.FavoriteAdapter
import com.example.travelplannerapp.utilities.FavoritesDbHelper

class FavoritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recycler = findViewById<RecyclerView>(R.id.recyclerFavorites)
        recycler.layoutManager = LinearLayoutManager(this)

        val db = FavoritesDbHelper(this)
        val routes = db.getAll().toMutableList()

        recycler.adapter = FavoriteAdapter(
            list = routes,

            // ✅ TAP → OPEN MAP WITH ROUTE
            onClick = { route ->
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("start_lat", route.startLat)
                    putExtra("start_lng", route.startLng)
                    putExtra("end_lat", route.endLat)
                    putExtra("end_lng", route.endLng)
                    putExtra("destination", route.destinationName)
                }
                startActivity(intent)
                finish()
            },

            // ✅ DELETE
            onDelete = { route ->
                db.delete(route.id)
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
