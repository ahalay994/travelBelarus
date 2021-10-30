package com.example.travel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.travel.adapters.BusAdapter
import com.example.travel.data.CurrentCity
import com.example.travel.data.yandex.rasp.Search
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_bus.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BusActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)

        /*** Тут мы получим город и текущие координаты ***/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100)
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val gcd = Geocoder(this, Locale.getDefault())
                    val addresses: List<Address> = gcd.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses.isNotEmpty()) {
                        // Вызываем функцию с получением текущего города
                        getCurrentCity(addresses[0])
                    } else {
                        Log.e("Location", "Месторасположение не найдено")
                    }
                    /*** Тут мы получили город и текущие координаты ***/
                }
            }

    }

    private fun getLink(method: String, params: String): String {
        return "${getString(R.string.ya_rasp_api_link)}${method}/?apikey=${getString(R.string.ya_rasp_api_key)}&${params}&lang=ru_RU";
    }

    private fun getCurrentCity(address: Address) {
        // Вызываем функицию с запросом на получение текущего города
        requestGetCity(getLink("nearest_settlement", "lat=${address.latitude}&lng=${address.longitude}&distance=50"))
    }

    private fun requestGetCity(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @SuppressLint("SimpleDateFormat")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val mapper = jacksonObjectMapper()
                    val currentCity = mapper.readValue<CurrentCity>(response.body!!.string())
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val currentDate = sdf.format(Date())
                    requestGetRoutes(getLink("search", "from=${currentCity.code}&to=c158&lang=ru_RU&transport_types=bus&transfers=true&date=${currentDate}"))
                }
            }
        })
    }

    fun requestGetRoutes(url: String) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val mapper = jacksonObjectMapper()
                    val search = mapper.readValue<Search>(response.body!!.string())
                    val segments = search.segments

                    runOnUiThread {
                        val adapter = BusAdapter(this@BusActivity, segments!!)
                        listView.adapter = adapter

                        listView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })
    }


    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        var currentLocation: Location? = null
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                }
            }
        return currentLocation
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