package com.example.travel.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import com.example.travel.R
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.util.Log
import android.view.View.GONE
import androidx.constraintlayout.widget.ConstraintSet.GONE
import com.google.android.material.card.MaterialCardView
import java.io.InputStream
import com.squareup.picasso.Picasso

import android.graphics.Color
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.widget.*
import com.example.travel.utility.LANGUAGE_DEFAULT
import com.example.travel.utility.PREF_DB_NAME
import com.example.travel.utility.PREF_TITLE_LANG

import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Transformation

class PlacesAdapter(
    private val context: Activity,
    private val id: Array<String>,
    private val tag_id: Array<String>,
    private val city_id: Array<String>,
    private val city_id_en: Array<String>,
    private val name: Array<String>,
    private val name_en: Array<String>,
    private val description: Array<String>,
    private val description_en: Array<String>,
    private val image: Array<String>,
    private val lat: Array<String>,
    private val lon: Array<String>,
    private val price: Array<String>)
    : ArrayAdapter<String>(context, R.layout.places_list, name) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val sharedPreferences = context.getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(PREF_TITLE_LANG, LANGUAGE_DEFAULT)

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.places_list, null, true)

        val imageView = rowView.findViewById(R.id.imageView) as ImageView
        val titleTextView = rowView.findViewById(R.id.titleTextView) as TextView
        val cityTextView = rowView.findViewById(R.id.cityTextView) as TextView
        val descriptionTextView = rowView.findViewById(R.id.descriptionTextView) as TextView

        if (image[position] !== "") {
            val uri = Uri.parse("android.resource://${context.packageName}/raw/${image[position]}")

            val transformation: Transformation = RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1f)
                .cornerRadiusDp(50f)
                .oval(false)
                .build()

            Picasso.get()
                .load(uri)
                .fit()
//                .transform(transformation)
                .into(imageView)
        }

        titleTextView.text = name[position]
        cityTextView.text = city_id[position]
        descriptionTextView.text = description[position]
        if (language == "en") {
            titleTextView.text = name_en[position]
            cityTextView.text = city_id_en[position]
            descriptionTextView.text = description_en[position]
        }

        return rowView
    }
}