package com.example.travelplannerapp.ui

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
        supportActionBar?.setHomeButtonEnabled(true)

        val recycler = findViewById<RecyclerView>(R.id.recyclerFavorites)
        recycler.layoutManager = LinearLayoutManager(this)

        val db = FavoritesDbHelper(this)
        val routes = db.getAll().toMutableList()

        recycler.adapter = FavoriteAdapter(routes) {
            db.delete(it.id)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()   // 👈 Goes back to MainActivity
        return true
    }
}
