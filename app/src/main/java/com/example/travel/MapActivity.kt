package com.example.travel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MapActivity : AppCompatActivity() {
    companion object {
        const val LAT = "lat"
        const val LON = "lon"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val lat = intent.getStringExtra(LAT)
        val lon = intent.getStringExtra(LON)
    }
}