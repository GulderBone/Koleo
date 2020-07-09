package com.gulderbone.koleo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val stringRequest = object: StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    stations = gson.fromJson(response, type)

                    stations.forEach{
                        println(it.name)
                    }
                },
                Response.ErrorListener { println("REQUEST FAILED") })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-KOLEO-Version"] = "1"
                return headers
            }
        }
        queue.add(stringRequest)

        find.setOnClickListener{
            val text = stationName.text.toString()
            val station = stations.first {
                it.name == text
            }
            println(station.latitude.toString())
            println(station.longitude.toString())

            latitude.text = station.latitude.toString()
            longitude.text = station.longitude.toString()
        }
    }
}