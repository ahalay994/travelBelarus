package com.example.travel.data.yandex.rasp.search

data class Segments (
    val arrival: String?,
    val from: Any?,
    val thread: Any?,
    val departure_platform: String?,
    val departure_from: Any?,
    val departure: String?,
    val stops: String?,
    val departure_terminal: Any?,
    val to: Any?,
    val transport_types: ArrayList<Any>?,
    val details: ArrayList<Any>?,
    val has_transfers: Boolean?,
    val transfers: ArrayList<Any>?,
    val tickets_info: Any?,
    val duration: Int?,
    val arrival_terminal: Any?,
    val start_date: String?,
    val arrival_platform: String?,
    val arrival_to: Any?
)
