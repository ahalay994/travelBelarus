package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.travel.adapters.PlaceGalleryAdapter
import com.example.travel.databinding.ActivityPlaceBinding
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.helpers.ZoomOutPageTransformer
import com.example.travel.models.PlacesModelClass
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.content_place.*

class PlaceActivity : AppCompatActivity() {
    companion object {
        const val ID = "id"
    }

    private val imagesList = mutableListOf<Int>()
    private lateinit var binding: ActivityPlaceBinding
    private val databaseHandler = DatabaseHandler(this)
    var id: Int = 0
    lateinit var place: PlacesModelClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra(ID, 0)
        place = databaseHandler.viewPlaceById(id)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = place.name
        binding.fab.setOnClickListener { view ->
            val placeIntent = Intent(this, MapActivity::class.java)
            placeIntent.putExtra(MapActivity.LAT, place.lat)
            placeIntent.putExtra(MapActivity.LON, place.lon)
            startActivity(placeIntent)
        }

        getData();
        createSlider();
    }

    fun getData() {
        cityName.text = place.city_name
        description.text = place.description
    }

    fun createSlider() {
        val wormDotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)

        for (i in 1..5) {
            addToList(R.drawable.doroga)
        }
        pager.adapter = PlaceGalleryAdapter(imagesList)

        val zoomOutPageTransformer = ZoomOutPageTransformer()
        pager.setPageTransformer { page, position ->
            zoomOutPageTransformer.transformPage(page, position)
        }

        wormDotsIndicator.setViewPager2(pager)
    }

    fun addToList(image: Int) {
        imagesList.add(image)
    }
}