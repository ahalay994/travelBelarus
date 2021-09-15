package com.example.travel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.models.PlacesModelClass

class PlacesActivity : AppCompatActivity() {
    companion object {
        const val ID = "id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        val id = intent.getIntExtra(ID, 0)

        val databaseHandler = DatabaseHandler(this)

        val place: PlacesModelClass = databaseHandler.viewPlaceById(id)
        Toast.makeText(applicationContext, "place.name: " + place.name, Toast.LENGTH_SHORT).show()
    }
}