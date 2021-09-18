package com.example.travel.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest

class GlobalHelper {
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        fun accessLocation(context: Context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
                return
            }
        }
    }

/*    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> accessLocation()
                PackageManager.PERMISSION_DENIED -> {

                }
            }
        }
    }*/

}