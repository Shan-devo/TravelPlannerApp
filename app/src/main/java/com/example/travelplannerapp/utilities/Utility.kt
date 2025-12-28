package com.example.travelplannerapp.utilities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

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
}
