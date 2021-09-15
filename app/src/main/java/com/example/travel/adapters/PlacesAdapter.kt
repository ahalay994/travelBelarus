package com.example.travel.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.travel.R
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.util.Log
import android.view.View.GONE
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintSet.GONE
import com.google.android.material.card.MaterialCardView
import java.io.InputStream
import com.squareup.picasso.Picasso

import android.graphics.Color
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with

import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Transformation


class PlacesAdapter(
    private val context: Activity,
    private val id: Array<String>,
    private val tag_id: Array<String>,
    private val city_id: Array<String>,
    private val name: Array<String>,
    private val description: Array<String>,
    private val image: Array<String>,
    private val lat: Array<String>,
    private val lon: Array<String>,
    private val price: Array<String>,
    private val viewType: Int)
    : ArrayAdapter<String>(context, R.layout.places_list, name) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.places_list, null, true)

        val viewList = rowView.findViewById(R.id.viewList) as RelativeLayout
        val cardView = rowView.findViewById(R.id.card) as MaterialCardView

        val placeImage = rowView.findViewById(R.id.placeImage) as ImageView
        val mtrl_list_item_icon = rowView.findViewById(R.id.mtrl_list_item_icon) as ImageView

        val placeName = rowView.findViewById(R.id.placeName) as TextView
        val mtrl_list_item_text = rowView.findViewById(R.id.mtrl_list_item_text) as TextView

        val cityName = rowView.findViewById(R.id.cityName) as TextView
        val mtrl_list_item_secondary_text = rowView.findViewById(R.id.mtrl_list_item_secondary_text) as TextView

        val placeDescription = rowView.findViewById(R.id.placeDescription) as TextView
        val mtrl_list_item_tertiary_text = rowView.findViewById(R.id.mtrl_list_item_tertiary_text) as TextView


        if (image[position] !== "") {
            val uri = Uri.parse("android.resource://${context.packageName}/drawable/${image[position]}")
            placeImage.setImageURI(null)
            placeImage.setImageURI(uri)

            val transformation: Transformation = RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1f)
                .cornerRadiusDp(50f)
                .oval(false)
                .build()

            Picasso.get()
                .load(uri)
                .fit()
                .transform(transformation)
                .into(mtrl_list_item_icon)
        }
        placeName.text = name[position]
        mtrl_list_item_text.text = name[position]

        cityName.text = city_id[position]
        mtrl_list_item_secondary_text.text = city_id[position]

        placeDescription.text = description[position]
        mtrl_list_item_tertiary_text.text = description[position]

        if (viewType == 0) {
            viewList.visibility = View.VISIBLE
            cardView.visibility = View.GONE
        } else {
            viewList.visibility = View.GONE
            cardView.visibility = View.VISIBLE
        }
        return rowView
    }
}