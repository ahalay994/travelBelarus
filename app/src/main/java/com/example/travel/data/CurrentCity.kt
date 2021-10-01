package com.example.travel.data

data class CurrentCity (
    val distance: Double,
    val code: String,
    val title: String,
    val popular_title: String,
    val short_title: String,
    val lat: Double,
    val lng: Double,
    val type: String
)