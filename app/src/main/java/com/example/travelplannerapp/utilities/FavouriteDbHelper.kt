package com.example.travelplannerapp.utilities

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.travelplannerapp.data.FavoriteRoute

class FavoritesDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                start_lat REAL NOT NULL,
                start_lng REAL NOT NULL,
                end_lat REAL NOT NULL,
                end_lng REAL NOT NULL,
                destination_name TEXT NOT NULL,
                distance_km REAL,
                duration_min INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /* ================= INSERT ================= */

    fun insert(route: FavoriteRoute) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_START_LAT, route.startLat)
            put(COL_START_LNG, route.startLng)
            put(COL_END_LAT, route.endLat)
            put(COL_END_LNG, route.endLng)
            put(COL_DEST_NAME, route.destinationName)
            put(COL_DISTANCE, route.distanceKm)
            put(COL_DURATION, route.durationMin)
        }

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    /* ================= READ ================= */

    fun getAll(): List<FavoriteRoute> {
        val list = mutableListOf<FavoriteRoute>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM favorite_routes ORDER BY id DESC",
            null
        )

        while (cursor.moveToNext()) {
            list.add(
                FavoriteRoute(
                    id = cursor.getLong(0),
                    startLat = cursor.getDouble(1),
                    startLng = cursor.getDouble(2),
                    endLat = cursor.getDouble(3),
                    endLng = cursor.getDouble(4),
                    destinationName = cursor.getString(5),
                    distanceKm = cursor.getDouble(6),
                    durationMin = cursor.getInt(7)
                )
            )
        }
        cursor.close()
        return list
    }

    /* ================= DELETE ================= */

    fun delete(id: Long) {
        val db = writableDatabase
        db.delete(
            TABLE_NAME,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
    }

    fun updateName(id: Long, newName: String) {
        val values = ContentValues().apply {
            put("destination_name", newName)
        }

        writableDatabase.update(
            TABLE_NAME,
            values,
            "id=?",
            arrayOf(id.toString())
        )
    }

    companion object {
        private const val DB_NAME = "favorites.db"
        private const val DB_VERSION = 1

        private const val TABLE_NAME = "favorite_routes"

        private const val COL_ID = "id"
        private const val COL_START_LAT = "start_lat"
        private const val COL_START_LNG = "start_lng"
        private const val COL_END_LAT = "end_lat"
        private const val COL_END_LNG = "end_lng"
        private const val COL_DEST_NAME = "destination_name"
        private const val COL_DISTANCE = "distance_km"
        private const val COL_DURATION = "duration_min"
    }
}
