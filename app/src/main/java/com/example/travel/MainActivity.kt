package com.example.travel

import android.content.Intent
import android.content.res.ColorStateList
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
import com.example.travel.`interface`.OnBottomSheetCallbacks
import com.example.travel.adapters.PlacesAdapter
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.models.PlacesModelClass
import com.example.travel.models.TagsModelClass
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_window.*

class MainActivity : AppCompatActivity() {
    private var listener: OnBottomSheetCallbacks? = null
    var optionsMenu: Menu? = null
    var actionbar: ActionBar? = null
    var VIEW_TYPE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionbar = supportActionBar
        if (actionbar !== null) {
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
        }
        actionbar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.purple_500)))
        //removing the shadow from the action bar
        supportActionBar?.elevation = 0f

        configureBackdrop()
        setToggleMenuButtons()
        setRangerSlider()

        getPlaces()
        addListChips()
    }

    fun setOnBottomSheetCallbacks(onBottomSheetCallbacks: OnBottomSheetCallbacks) {
        this.listener = onBottomSheetCallbacks
    }

    fun closeBottomSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun openBottomSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    private fun setToggleMenuButtons() {
        materialButtonToggleGroupSort.addOnButtonCheckedListener { _, checkedId, _ ->
            toggleButton(findViewById(checkedId))
        }
    }

    private fun setRangerSlider() {
        lengthSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed
            lengthTextView.text = "${rangeSlider.values[0].toInt()} BYN - ${rangeSlider.values[1].toInt()} BYN"
            if (rangeSlider.values[1].toInt() == 150) {
                lengthTextView.text = lengthTextView.text.toString() + "+"
            }
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

    private fun handleCheckedButtonsInSort() {
        when {
            materialButtonToggleGroupSort.checkedButtonIds.contains(R.id.mostPopularButton) -> {

            }
            materialButtonToggleGroupSort.checkedButtonIds.contains(R.id.closestButton) -> {

            }
        }
    }

    private fun toggleButton(button: MaterialButton) {
        if (button.textColors.defaultColor == ContextCompat.getColor(this, R.color.white)) {
            button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.selected_item))
            button.setTextColor(ContextCompat.getColor(this, R.color.selected_item))
        } else {
            button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

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
            R.id.action_view -> {
                if (VIEW_TYPE == 0) {
                    viewList.numColumns = 2
                    VIEW_TYPE = 1
                    getPlaces()
                } else {
                    viewList.numColumns = 1
                    VIEW_TYPE = 0
                    getPlaces()
                }
                true
            }
            R.id.action_settings -> {
                Intent(applicationContext, ConfigActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            R.id.action_configuration -> {
//                val configActivity = Intent(this, ConfigActivity::class.java)
//                startActivity(configActivity)
                actionbar!!.setDisplayHomeAsUpEnabled(true)
                item.setVisible(false)
                closeBottomSheet()
                true
            }
            R.id.action_exit -> {
                Toast.makeText(applicationContext, "click on exit", Toast.LENGTH_LONG).show()
                return true
            }
            android.R.id.home -> {
//                Toast.makeText(this, "Home", Toast.LENGTH_LONG).show()
                optionsMenu!!.findItem(R.id.action_configuration).setVisible(true)
                actionbar!!.setDisplayHomeAsUpEnabled(false)
                openBottomSheet()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getPlaces() {
        val databaseHandler = DatabaseHandler(this)

        val place: List<PlacesModelClass> = databaseHandler.viewPlace()
        val placeArrayId = Array(place.size){"0"}
        val placeArrayTagId = Array(place.size){"null"}
        val placeArrayCityId = Array(place.size){"0"}
        val placeArrayName = Array(place.size){"null"}
        val placeArrayDescription = Array(place.size){"null"}
        val placeArrayImage = Array(place.size){"null"}
        val placeArrayLat = Array(place.size){"null"}
        val placeArrayLon = Array(place.size){"null"}
        val placeArrayPrice = Array(place.size){"null"}
        var index = 0
        for(e in place) {
            placeArrayId[index] = e.id.toString()
            placeArrayTagId[index] = e.tag_id
            placeArrayCityId[index] = e.city_name
            placeArrayName[index] = e.name
            placeArrayDescription[index] = e.description
            placeArrayImage[index] = e.image
            placeArrayLat[index] = e.lat
            placeArrayLon[index] = e.lon
            placeArrayPrice[index] = e.price.toString()

            index++
        }

        val placesAdapter = PlacesAdapter(this,placeArrayId,placeArrayTagId,placeArrayCityId,placeArrayName,placeArrayDescription,placeArrayImage,placeArrayLat,placeArrayLon, placeArrayPrice, VIEW_TYPE)
        viewList.adapter = placesAdapter

        viewList.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            val placeIntent = Intent(this, PlaceActivity::class.java)
            placeIntent.putExtra(PlaceActivity.ID, placeArrayId[position].toInt())
            startActivity(placeIntent)
        })
    }

    private fun addListChips() {
        try {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
            val inflater = LayoutInflater.from(this)

            val databaseHandler = DatabaseHandler(this)

            val tag: List<TagsModelClass> = databaseHandler.viewTag()
            val tagArrayId = Array<String>(tag.size){"0"}
            val tagArrayTitle = Array<String>(tag.size){"null"}
            val tagArrayActive = Array<String>(tag.size){"null"}
            var index = 0
            for(e in tag){
                tagArrayId[index] = e.tagId.toString()
                tagArrayTitle[index] = e.tagName
                tagArrayActive[index] = e.tagActive.toString()

                val chip = inflater.inflate(R.layout.layout_chip_entry, chipGroup, false) as Chip
                chip.text = e.tagName
                chip.isChecked = e.tagActive == 1

                chipGroup.addView(chip)

                chip.setOnClickListener {
                    // Responds to chip click
                    Log.i("setOnClickListener", it.toString())
                }

                chip.setOnCloseIconClickListener {
                    // Responds to chip's close icon click if one is present
                    Log.i("setOnCloseIconClickList", it.toString())
                }

                chip.setOnCheckedChangeListener { chip, isChecked ->
                    Log.i("id", chip.id.toString())
                    // Responds to chip checked/unchecked
                    chip?.let { chipView ->
                        Toast.makeText(this, chip.text, Toast.LENGTH_SHORT).show()
                    } ?: kotlin.run {
                    }
                }

                index++
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
            Log.e("Error", "Error: " + e.message)
        }
    }
}
