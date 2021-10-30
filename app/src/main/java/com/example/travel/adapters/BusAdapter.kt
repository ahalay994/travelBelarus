package com.example.travel.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.travel.R
import com.example.travel.data.yandex.rasp.search.Segments
import com.example.travel.data.yandex.rasp.search.segments.Station
import com.example.travel.data.yandex.rasp.search.segments.details.Transfer
import com.example.travel.data.yandex.rasp.search.segments.thread.Carrier
import com.example.travel.data.yandex.rasp.search.segments.Thread
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class BusAdapter(private var context: Context?, private var data: ArrayList<Any>) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    @SuppressLint("NewApi", "SimpleDateFormat", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) view = LayoutInflater.from(context).inflate(R.layout.item_ya_list, parent, false)

        val mapper = jacksonObjectMapper()
        /*** У нас есть полный сегмент ***/
        val segment = mapper.readValue<Segments>(Gson().toJson(this.getItem(position)))

        if (segment.has_transfers == false) {
            view!!.findViewById<LinearLayout>(R.id.no_transfers).visibility = View.VISIBLE
            view.findViewById<LinearLayout>(R.id.transfers).visibility = View.GONE
            /** Получили полноценный трейд **/
            val thread = mapper.readValue<Thread>(Gson().toJson(segment.thread))
            /** Получаю данные о компании перевозчике **/
            val carrier = mapper.readValue<Carrier>(Gson().toJson(thread.carrier))
            var logo = carrier.logo
            if (logo == null) logo = carrier.logo_svg
            /** Получа данные о станции отправления **/
            val fromStation = mapper.readValue<Station>(Gson().toJson(segment.from))
            val toStation = mapper.readValue<Station>(Gson().toJson(segment.to))

            val departure = LocalDateTime.parse(segment.departure, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))
            val arrival = LocalDateTime.parse(segment.arrival, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))

            /** Заполняем layout **/
            view.findViewById<TextView>(R.id.title).text = "${thread.title} (${getType(thread.transport_type)})"
            view.findViewById<TextView>(R.id.departure).text = "Время отправления: $departure (${fromStation.title})"
            view.findViewById<TextView>(R.id.arrival).text = "Время прибытия: $arrival (${toStation.title})"
            view.findViewById<TextView>(R.id.number).text = "Номер: ${thread.number}"

            Picasso.get()
                .load(theme(thread.transport_type))
                .error(R.drawable.ic_launcher_background)
                .into(view.findViewById<ImageView>(R.id.logo))
        }

        if (segment.has_transfers == true && segment.details!!.size == 3) {
            view!!.findViewById<LinearLayout>(R.id.no_transfers).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.transfers).visibility = View.VISIBLE

            val detailFrom = mapper.readValue<Segments>(Gson().toJson(segment.details[0]))
            val departureFromStation = mapper.readValue<Station>(Gson().toJson(segment.departure_from))
            /*** ***/
            val departureFrom = LocalDateTime.parse(detailFrom.departure, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))
            val arrivalFrom = LocalDateTime.parse(detailFrom.arrival, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))
            /*** ***/
            val fromStation1 = mapper.readValue<Station>(Gson().toJson(detailFrom.from))
            val toStation1 = mapper.readValue<Station>(Gson().toJson(detailFrom.to))
            val thread1 = mapper.readValue<Thread>(Gson().toJson(detailFrom.thread))
            val carrier1 = mapper.readValue<Carrier>(Gson().toJson(thread1.carrier))

            val detailTransfer = mapper.readValue<Transfer>(Gson().toJson(segment.details[1]))
            val transferTo = mapper.readValue<Station>(Gson().toJson(detailTransfer.transfer_to))

            val detailTo = mapper.readValue<Segments>(Gson().toJson(segment.details[2]))
            val arrivalToStation = mapper.readValue<Station>(Gson().toJson(segment.arrival_to))
            /*** ***/
            val departureTo = LocalDateTime.parse(detailTo.departure, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))
            val arrivalTo = LocalDateTime.parse(detailTo.arrival, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ")).format(DateTimeFormatter.ofPattern("HH:mm"))
            /*** ***/
            val toStation2 = mapper.readValue<Station>(Gson().toJson(detailTo.to))
            val thread2 = mapper.readValue<Thread>(Gson().toJson(detailTo.thread))
            val carrier2 = mapper.readValue<Carrier>(Gson().toJson(thread2.carrier))


            var departureFromStationTitle = departureFromStation.title
            if (departureFromStationTitle === "") departureFromStationTitle = "${departureFromStation.popular_title} - ${departureFromStation.station_type_name}"

            var transferToTitle = transferTo.title
            if (transferToTitle === "") transferToTitle = "${transferTo.popular_title} - ${transferTo.station_type_name}"

            var arrivalToStationTitle = arrivalToStation.title
            if (arrivalToStationTitle === "") arrivalToStationTitle = "${arrivalToStation.popular_title} - ${arrivalToStation.station_type_name}"

            /*** Получа данные о станции отправления ***/
            view.findViewById<TextView>(R.id.main_title).text = "${fromStation1.title} - ${toStation1.title} - ${toStation2.title}"

            view.findViewById<TextView>(R.id.first_transfer_title).text = "${thread1.title}"
            view.findViewById<TextView>(R.id.first_transfer_time_from).text = "Время отправления: $departureFrom (${departureFromStationTitle})"
            view.findViewById<TextView>(R.id.first_transfer_time_to).text = "Время прибытия: $arrivalFrom (${transferToTitle})"

            if (carrier1 === null) view.findViewById<TextView>(R.id.first_transfer_number).visibility = View.GONE else view.findViewById<TextView>(R.id.first_transfer_number).text = "${carrier1.title}"

            view.findViewById<TextView>(R.id.second_transfer_title).text = "${thread2.title}"
            view.findViewById<TextView>(R.id.second_transfer_time_from).text = "Время отправления: $departureTo (${transferToTitle})"
            view.findViewById<TextView>(R.id.second_transfer_time_to).text = "Время прибытия: $arrivalTo (${arrivalToStationTitle})"
            if (carrier2 === null) {
                view.findViewById<TextView>(R.id.second_transfer_number).visibility = View.GONE
            } else {
                view.findViewById<TextView>(R.id.second_transfer_number).text = "${carrier2.title}"
            }

            Picasso.get()
                .load(theme(thread1.transport_type))
                .error(R.drawable.ic_launcher_background)
                .into(view.findViewById<ImageView>(R.id.logo))

            view.findViewById<ImageView>(R.id.logo1).visibility = View.VISIBLE
            Picasso.get()
                .load(theme(thread2.transport_type))
                .error(R.drawable.ic_launcher_background)
                .into(view.findViewById<ImageView>(R.id.logo1))
        }

        return view!!
    }

    private fun getType(transportType: String?): String {
        return when (transportType) {
            "train" -> "Поезд"
            "suburban" -> "Электричка"
            "bus" -> "Автобус"
            else -> ""
        }
    }

    private fun theme(type: String?): Int {
        val nightModeFlags: Configuration = context!!.getResources().getConfiguration()
        val uiMode = nightModeFlags.uiMode
        return when(type) {
            "train" -> {
                when(uiMode) {
                    33 -> R.drawable.train_white
                    17 -> R.drawable.train_black
                    else -> R.drawable.train_gray
                }
            }
            "suburban" -> {
                when(uiMode) {
                    33 -> R.drawable.suburban_white
                    17 -> R.drawable.suburban_black
                    else -> R.drawable.suburban_gray
                }
            }
            "bus" -> {
                when(uiMode) {
                    33 -> R.drawable.bus_white
                    17 -> R.drawable.bus_black
                    else -> R.drawable.bus_gray
                }
            }
            else -> {
                when(uiMode) {
                    33 -> R.drawable.car_white
                    17 -> R.drawable.car_black
                    else -> R.drawable.car_gray
                }
            }
        }
    }
}