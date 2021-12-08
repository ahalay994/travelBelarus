package com.example.travel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.travel.adapters.PlaceGalleryAdapter
import com.example.travel.databinding.ActivityPlaceBinding
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.helpers.ZoomOutPageTransformer
import com.example.travel.models.PlacesModelClass
import com.example.travel.utility.LANGUAGE_DEFAULT
import com.example.travel.utility.PREF_DB_NAME
import com.example.travel.utility.PREF_TITLE_LANG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.content_place.*
import kotlinx.android.synthetic.main.fragment_window.*
import java.util.*

class PlaceActivity : AppCompatActivity() {
    companion object {
        const val ID = "id"
    }

    private var isCheckedVisited = false
    private val imagesList = mutableListOf<Int>()
    private lateinit var binding: ActivityPlaceBinding
    private val databaseHandler = DatabaseHandler(this)
    var id: Int = 0
    lateinit var place: PlacesModelClass
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(PREF_TITLE_LANG, LANGUAGE_DEFAULT)

        binding = ActivityPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra(ID, 0)
        place = databaseHandler.viewPlaceById(id)

        if (place.visited == 1) {
            isCheckedVisited = true
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        var name = place.name
        var description = place.description
        var nameCity = place.city_name
        var textMinibus = place.minibus_text

        if (language == "en") {
            name = place.name_en
            description = place.description_en
            nameCity = place.city_name_en
            textMinibus = place.minibus_text_en
        }

        binding.toolbarLayout.title = name

        var checkCurrentCity = false
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val gcd = Geocoder(this, Locale.getDefault())
                    val addresses: List<Address> = gcd.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses.isNotEmpty()) {
                        if (addresses[0].locality.equals(place.city_name, true)) {
                            checkCurrentCity = true
                        }
                    } else {
                        Log.e("Location", "Месторасположение не найдено")
                    }
                    /*** Тут мы получили город и текущие координаты ***/
                }
            }

//        if (place.is_bus == 0 || !checkCurrentCity || !isNetworkConnected()) {
//            binding.fabBus.visibility = View.GONE
//        }
//        if (place.is_train == 0 || !checkCurrentCity || !isNetworkConnected()) {
//            binding.fabTrain.visibility = View.GONE
//        }
//        if (place.is_bus == 0 || !checkCurrentCity || !isNetworkConnected()) {
//            binding.fabBus.visibility = View.GONE
//        }
//        if (place.is_minibus == 0 || !checkCurrentCity || !isNetworkConnected()) {
//            binding.fabMinibus.visibility = View.GONE
//        }

        binding.fabMap.setOnClickListener { view ->
            val placeIntent = Intent(this, MapActivity::class.java)
            placeIntent.putExtra(MapActivity.NAME, name)
            placeIntent.putExtra(MapActivity.LAT, place.lat)
            placeIntent.putExtra(MapActivity.LON, place.lon)
            closeFabs()
            startActivity(placeIntent)
        }

        binding.fabTrain.setOnClickListener { view ->
            val placeIntent = Intent(this, TrainActivity::class.java)
            placeIntent.putExtra(TrainActivity.NAME, name)
            startActivity(placeIntent)
        }

        binding.fabBus.setOnClickListener { view ->
            val placeIntent = Intent(this, BusActivity::class.java)
            placeIntent.putExtra(TrainActivity.NAME, name)
            startActivity(placeIntent)
        }

        binding.fabMinibus.setOnClickListener { view ->
            val placeIntent = Intent(this, MinibusActivity::class.java)
            placeIntent.putExtra(MinibusActivity.NAME, name)
            placeIntent.putExtra(MinibusActivity.TEXT, textMinibus)
            startActivity(placeIntent)
        }

        binding.fab.setOnClickListener {
            if (binding.fab.isExpanded) {
                closeFabs()
            } else {
                openFabs()
            }
        }

        getData(description, nameCity)
        createSlider()

    }

    fun closeFabs() {
        binding.fab.setImageResource(R.drawable.route)
        fab.isExpanded = !fab.isExpanded
    }

    fun openFabs() {
        binding.fab.setImageResource(R.drawable.ic_baseline_close_black_24)
        fab.isExpanded = !fab.isExpanded
    }

    fun getData(descriptionText: String, nameCity: String) {
        cityName!!.text = nameCity
        description!!.text = descriptionText
    }

    fun createSlider() {
        val wormDotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)

        for (i in 1..5) {
            addToList(R.raw.kostel_svyatoy_troiti)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top_place, menu)
        menu.findItem(R.id.visited).setChecked(isCheckedVisited)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.visited -> {
                isCheckedVisited = !isCheckedVisited;
                item.setChecked(isCheckedVisited)

                val databaseHandler = DatabaseHandler(this)
                databaseHandler.updateVisited(id, isCheckedVisited)

                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnected(): Boolean {
        //1
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //2
        val activeNetwork = connectivityManager.activeNetwork
        //3
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        //4
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}