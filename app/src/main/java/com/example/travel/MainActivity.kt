package com.example.travel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import com.example.travel.`interface`.OnBottomSheetCallbacks
import com.example.travel.adapters.PlacesAdapter
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.helpers.SharedPreference
import com.example.travel.models.PlacesModelClass
import com.example.travel.models.TagsModelClass
import com.example.travel.utility.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_window.viewList
import java.util.Locale

import com.example.travel.*
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate

import com.example.travel.*
import com.example.travel.*

class MainActivity : AppCompatActivity() {
    final val PREF_SORT = "SORT";
    final val PREF_PRICE_MIN = "PRICE_MIN"
    final val PREF_PRICE_MAX = "PRICE_MAX"

    private var listener: OnBottomSheetCallbacks? = null
    var optionsMenu: Menu? = null
    var actionbar: ActionBar? = null

    var filerTags: MutableMap<Int, Int> = HashMap()
    var filterSort = 0
    var filterPrice = arrayListOf<Int>(0, 0)

    var sharedPreference: SharedPreference? = null

    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val localeService = LocaleService
        localeService.updateBaseContextLocale(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreference = SharedPreference(this)

        actionbar = supportActionBar
        if (actionbar !== null) {
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
        }
        supportActionBar!!.title = getString(R.string.app_name)
        //removing the shadow from the action bar
        supportActionBar?.elevation = 0f

        if (sharedPreference?.getValueInt(PREF_SORT) == 0) {
            materialButtonToggleGroupSort.check(R.id.mostPopularButton)
            toggleButton(findViewById(R.id.mostPopularButton))
        } else {
            materialButtonToggleGroupSort.check(R.id.closestButton)
            toggleButton(findViewById(R.id.closestButton))
        }

        configureBackdrop()
        setToggleMenuButtons()
        setRangerSlider()
        handleCheckedButtonsInSort()

        init()

        submitPlaces()
    }

    private fun init() {
        addListChips()
        getPlaces()
    }

    fun setOnBottomSheetCallbacks(onBottomSheetCallbacks: OnBottomSheetCallbacks) {
        this.listener = onBottomSheetCallbacks
    }

    fun openBottomSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        if (actionbar !== null) {
            actionbar!!.setDisplayHomeAsUpEnabled(true)
            optionsMenu!!.findItem(R.id.action_configuration).setVisible(false)
        }
    }

    fun closeBottomSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        if (actionbar !== null) {
            optionsMenu!!.findItem(R.id.action_configuration).setVisible(true)
            actionbar!!.setDisplayHomeAsUpEnabled(false)
        }
    }

    // Клик по кнопкам в контейнере
    private fun setToggleMenuButtons() {
        materialButtonToggleGroupSort.addOnButtonCheckedListener { group, checkedId, isChecked ->
            filterSort = if (checkedId == R.id.mostPopularButton) 0 else 1
            toggleButton(findViewById(checkedId))
        }
    }

    // Изменение ренджбара (стоимости)
    @SuppressLint("SetTextI18n")
    private fun setRangerSlider() {
        val databaseHandler = DatabaseHandler(this)
        val price = databaseHandler.price()

        var priceMin = 0
        var priceMax = 0

        val storagePriseMin = sharedPreference?.getValueInt(PREF_PRICE_MIN)
        val storagePriseMax = sharedPreference?.getValueInt(PREF_PRICE_MAX)

        if (storagePriseMin == null && storagePriseMax == null) {
            sharedPreference?.save(PREF_PRICE_MIN, price[0])
            sharedPreference?.save(PREF_PRICE_MAX, price[1])
        }
        priceMin = sharedPreference?.getValueInt(PREF_PRICE_MIN)!!
        priceMax = sharedPreference?.getValueInt(PREF_PRICE_MAX)!!

        if (price[0] > priceMin) priceMin = price[0]
        if (price[1] < priceMax || priceMax == 0) priceMax = price[1]

        lengthSlider.valueFrom = price[0].toFloat()
        lengthSlider.valueTo = price[1].toFloat()
        lengthSlider.values = arrayOf(priceMin.toFloat(), priceMax.toFloat()).toMutableList()
        lengthTextView.text = "${priceMin.toFloat()} BYN - ${priceMax.toFloat()} BYN"

        filterPrice[0] = priceMin
        filterPrice[1] = priceMax

        lengthSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            lengthTextView.text = "${rangeSlider.values[0].toInt()} BYN - ${rangeSlider.values[1].toInt()} BYN"

            filterPrice[0] = rangeSlider.values[0].toInt()
            filterPrice[1] = rangeSlider.values[1].toInt()
        }

        lengthSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being stopped
            }
        })
    }

    // Клик по кнопкам сортировки
    private fun handleCheckedButtonsInSort() {
        when {
            materialButtonToggleGroupSort.checkedButtonIds.contains(R.id.mostPopularButton) -> {
                filterSort = 0
            }
            materialButtonToggleGroupSort.checkedButtonIds.contains(R.id.closestButton) -> {
                filterSort = 1
            }
        }
    }

    // Функция изменения цвета кнопок
    private fun toggleButton(button: MaterialButton) {
        if (button.textColors.defaultColor == ContextCompat.getColor(this, R.color.white)) {
            button.strokeColor =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.selected_item))
            button.setTextColor(ContextCompat.getColor(this, R.color.selected_item))
        } else {
            button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    // Выпадающая шторка
    private fun configureBackdrop() {
        val fragment = supportFragmentManager.findFragmentById(R.id.filter_fragment)

        fragment?.view?.let {
            BottomSheetBehavior.from(it).let { bs ->

                bs.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        listener?.onStateChanged(bottomSheet, newState)
                    }
                })

                bs.state = BottomSheetBehavior.STATE_EXPANDED

                mBottomSheetBehavior = bs
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                closeBottomSheet()
                supportActionBar?.elevation = 0f
                Intent(applicationContext, SettingsActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            R.id.action_configuration -> {
                openBottomSheet()
                true
            }
            android.R.id.home -> {
                closeBottomSheet()
                true
            }
            R.id.action_exit -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Получить места
    fun getPlaces() {
        val databaseHandler = DatabaseHandler(this)

        val place: List<PlacesModelClass> = databaseHandler.viewPlace(
            sharedPreference!!.getValueInt(PREF_SORT),
            sharedPreference!!.getValueInt(PREF_PRICE_MIN),
            sharedPreference!!.getValueInt(PREF_PRICE_MAX)
        )
        val placeArrayId = Array(place.size) { "0" }
        val placeArrayTagId = Array(place.size) { "null" }
        val placeArrayCityId = Array(place.size) { "0" }
        val placeArrayCityIdEn = Array(place.size) { "0" }
        val placeArrayName = Array(place.size) { "null" }
        val placeArrayNameEn = Array(place.size) { "null" }
        val placeArrayDescription = Array(place.size) { "null" }
        val placeArrayDescriptionEn = Array(place.size) { "null" }
        val placeArrayImage = Array(place.size) { "null" }
        val placeArrayLat = Array(place.size) { "null" }
        val placeArrayLon = Array(place.size) { "null" }
        val placeArrayPrice = Array(place.size) { "null" }
        var index = 0
        for (e in place) {
            placeArrayId[index] = e.id.toString()
            placeArrayTagId[index] = e.tag_id
            placeArrayCityId[index] = e.city_name
            placeArrayCityIdEn[index] = e.city_name_en
            placeArrayName[index] = e.name
            placeArrayNameEn[index] = e.name_en
            placeArrayDescription[index] = e.description
            placeArrayDescriptionEn[index] = e.description_en
            placeArrayImage[index] = e.image
            placeArrayLat[index] = e.lat
            placeArrayLon[index] = e.lon
            placeArrayPrice[index] = e.price.toString()

            index++
        }

        val placesAdapter = PlacesAdapter(
            this,
            placeArrayId,
            placeArrayTagId,
            placeArrayCityId,
            placeArrayCityIdEn,
            placeArrayName,
            placeArrayNameEn,
            placeArrayDescription,
            placeArrayDescriptionEn,
            placeArrayImage,
            placeArrayLat,
            placeArrayLon,
            placeArrayPrice
        )
        viewList.adapter = placesAdapter

        viewList.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            moreCLick(PlaceActivity.ID, placeArrayId[position].toInt())
        })

    }

    private fun moreCLick(id: String, _id: Int) {
        val placeIntent = Intent(this, PlaceActivity::class.java)
        placeIntent.putExtra(id, _id)
        startActivity(placeIntent)
    }

    // Получить теги
    private fun addListChips() {
        try {
            chipGroup.removeAllViews()
            val inflater = LayoutInflater.from(this)

            val databaseHandler = DatabaseHandler(this)
            val tag: List<TagsModelClass> = databaseHandler.viewTag()

            for (e in tag) {
                val chip = inflater.inflate(R.layout.layout_chip_entry, chipGroup, false) as Chip

                chip.id = e.tagId
                chip.text = e.tagName
                chip.isChecked = e.tagActive == 1
                chipGroup.addView(chip)

                filerTags[chip.id] = e.tagActive

                chip.setOnCheckedChangeListener { chip, isChecked ->
                    chip?.let { chipView ->
                        if (isChecked) filerTags[chip.id] = 1 else filerTags[chip.id] = 0
                    } ?: kotlin.run {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
            Log.e("Error", "Error: " + e.message)
        }
    }

    fun submitPlaces() {
        submitFilter.setOnClickListener {
            sharedPreference?.save(PREF_PRICE_MIN, filterPrice[0])
            sharedPreference?.save(PREF_PRICE_MAX, filterPrice[1])
            sharedPreference?.save(PREF_SORT, filterSort)

            val databaseHandler = DatabaseHandler(this)

            databaseHandler.updateTag(1, filerTags[1]!!)
            databaseHandler.updateTag(2, filerTags[2]!!)
            databaseHandler.updateTag(3, filerTags[3]!!)
            databaseHandler.updateTag(4, filerTags[4]!!)
            databaseHandler.updateTag(5, filerTags[5]!!)
            databaseHandler.updateTag(6, filerTags[6]!!)
            databaseHandler.updateTag(7, filerTags[7]!!)
            databaseHandler.updateTag(8, filerTags[8]!!)
            databaseHandler.updateTag(9, filerTags[9]!!)
            databaseHandler.updateTag(10, filerTags[10]!!)
            databaseHandler.updateTag(11, filerTags[11]!!)
            databaseHandler.updateTag(12, filerTags[12]!!)
            databaseHandler.updateTag(13, filerTags[13]!!)
            databaseHandler.updateTag(14, filerTags[14]!!)
            databaseHandler.updateTag(15, filerTags[15]!!)

            init()
            closeBottomSheet()
        }
    }
}
