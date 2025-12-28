package com.example.travelplannerapp.utilities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.travelplannerapp.R

object Utility {

    fun hideKeyboard(activity: Activity, token: android.os.IBinder) {
        val imm =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE)
                    as InputMethodManager
        imm.hideSoftInputFromWindow(token, 0)
    }

    fun drawableToBitmap(activity: Activity, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(activity, drawableId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

     fun weatherIcon(symbol: String): Int {
        return when {
            symbol.contains("clearsky") -> R.drawable.ic_weather_sun
            symbol.contains("partlycloudy") -> R.drawable.ic_weather_cloud
            symbol.contains("cloudy") -> R.drawable.ic_weather_cloud
            symbol.contains("rain") -> R.drawable.ic_weather_rain
            symbol.contains("snow") -> R.drawable.ic_weather_snow
            symbol.contains("fog") -> R.drawable.ic_weather_fog
            else -> R.drawable.ic_weather_cloud
        }
    }

    fun formatDuration(minutes: Int): String {
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hrs = minutes / 60
            val mins = minutes % 60
            if (mins == 0) "$hrs hr"
            else "${hrs} hr ${mins} min"
        }
    }

}
