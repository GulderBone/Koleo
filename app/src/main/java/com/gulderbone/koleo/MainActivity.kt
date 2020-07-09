package com.gulderbone.koleo

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gulderbone.koleo.models.Station
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson = Gson()
        val type = object : TypeToken<Array<Station>>() {}.type

        val queue = Volley.newRequestQueue(this)
        val url = "https://koleo.pl/api/v2/main/stations"

        lateinit var stations: Array<Station>

        val stringRequest = object : StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                stations = gson.fromJson(response, type)

                stations.forEach {
                    println(it.name)
                }
            },
            Response.ErrorListener { println("REQUEST FAILED") }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-KOLEO-Version"] = "1"
                return headers
            }
        }
        queue.add(stringRequest)

        find.setOnClickListener {
            val fromStationText = fromStationInput.editText?.text.toString()
            val toStationText = toStationInput.editText?.text.toString()
            val fromStation = stations.firstOrNull {
                it.name == fromStationText
            }
            val toStation = stations.firstOrNull {
                it.name == toStationText
            }
            if (fromStation != null && toStation != null) {
                val distance = calculateDistanceBetweenStations(fromStation, toStation)
                distanceTextView.text = String.format("%.2f", distance) + " km"
            } else {
                distanceTextView.text = "wiel błąd"
            }
        }
    }

    private fun calculateDistanceBetweenStations(stationA: Station, stationB: Station): Double {
        val stationALocation = Location("")
        stationALocation.latitude = stationA.latitude
        stationALocation.longitude = stationA.longitude

        val stationBLocation = Location("")
        stationBLocation.latitude = stationB.latitude
        stationBLocation.longitude = stationB.longitude

        return stationALocation.distanceTo(stationBLocation).toDouble() / 1000 // m -> km
    }
}