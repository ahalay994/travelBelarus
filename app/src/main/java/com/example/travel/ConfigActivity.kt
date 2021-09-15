package com.example.travel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.models.TagsModelClass
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.activity_config.*
import java.text.NumberFormat
import java.util.*

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        addListChips()

        slider.values = listOf(10.0F, 40.0F)
        slider.valueFrom = 0.0F;
        slider.valueTo = 50.0F;

        slider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                println("Start Tracking Touch")
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                println("Stop Tracking Touch")
            }
        })

        slider.addOnChangeListener { rangeSlider, value, fromUser ->
            println(rangeSlider)
            println(value)
            println(fromUser)
        }

        slider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("BYN")
            format.format(value.toDouble())
        }
    }


    private fun addListChips() {
        try {
            val inflater = LayoutInflater.from(this)

            val databaseHandler = DatabaseHandler(this)

            val tag: List<TagsModelClass> = databaseHandler.viewTag()
//            val tagArrayId = Array<String>(tag.size){"0"}
//            val tagArrayTitle = Array<String>(tag.size){"null"}
//            val tagArrayActive = Array<String>(tag.size){"null"}
//            var index = 0
            for(e in tag){
//                tagArrayId[index] = e.tagId.toString()
//                tagArrayTitle[index] = e.tagName
//                tagArrayActive[index] = e.tagActive.toString()

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

//                index++
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun saveRecord() {
//        val id = 1
//        val title = "Природа"
//        val isActive = 0
        val databaseHandler = DatabaseHandler(this)

//        val status = databaseHandler.addTag(TagsModelClass(id,title, isActive))
        val status = databaseHandler.addTag()
        if(status > -1){
            Toast.makeText(applicationContext,"record save", Toast.LENGTH_LONG).show()
        }
        val statusRegions = databaseHandler.addRegion()
        if(statusRegions > -1){
            Toast.makeText(applicationContext,"record save", Toast.LENGTH_LONG).show()
        }
    }

    fun viewTags(){
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
            index++

            Log.i("tagArrayId", e.tagId.toString())
            Log.i("tagArrayTitle", e.tagName)
            Log.i("tagArrayActive", e.tagActive.toString())
        }

        //creating custom ArrayAdapter
//        val myListAdapter = MyListAdapter(this,tagArrayId,tagArrayTitle,tagArrayActive)
//        listView.adapter = myListAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}