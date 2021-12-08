package com.example.travel.models

class PlacesModelClass(
    var id: Int,
    var tag_id: String,
    var city_name: String,
    var city_name_en: String,
    var is_car: Int,
    var is_train: Int,
    var is_bus: Int,
    var is_minibus: Int,
    var minibus_text: String,
    var minibus_text_en: String,
    var name: String,
    var name_en: String,
    var description: String,
    var description_en: String,
    var image: String,
    var lat: String,
    var lon: String,
    var price: Float,
    var visited: Int
)