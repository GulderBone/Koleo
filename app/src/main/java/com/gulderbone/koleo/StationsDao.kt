package com.gulderbone.koleo

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gulderbone.koleo.models.Station
import java.io.File
import java.lang.reflect.Type
import java.time.Instant

class StationsDao(context: Context) {

    lateinit var stations: Array<Station>

    val stationsArrayType: Type = object : TypeToken<Array<Station>>() {}.type
    private val stationsFilename = "stations.json"

    private val updateTimePreference = "updateTime"
    private val cacheTtl = 24 * 60 * 60 // 24h

    private val stationsDataFile: File
    private val preferences: SharedPreferences =
        context.getSharedPreferences("cache", Context.MODE_PRIVATE)
    private val gson: Gson = Gson()
    private val requestQueue = Volley.newRequestQueue(context)

    init {
        stationsDataFile = File(context.filesDir, stationsFilename)
    }


    fun getStationsData(callback: ApiRequestCallback) {
        if (!stationsDataFile.exists() || shouldUpdateData()) {
            getStationsDataFromApi(callback)
        } else {
            getStationsDataFromCachedFile(callback)
        }
    }

    private fun updateCacheExpirationTime() {
        with(preferences.edit()) {
            putLong(updateTimePreference, Instant.now().epochSecond)
            commit()
        }
    }

    private fun shouldUpdateData(): Boolean {
        if (preferences.contains(updateTimePreference)) {
            val lastUpdateTime = preferences.getLong(updateTimePreference, 0)
            if (lastUpdateTime != 0L) {
                val updateTimeDifference = Instant.now().epochSecond - lastUpdateTime
                return updateTimeDifference > cacheTtl
            }
        }
        return false
    }

    private fun saveStationsData(stationsData: String) {
        stationsDataFile.writeText(stationsData)
    }

    private fun getStationsDataFromApi(callback: ApiRequestCallback) {
        val url = "https://koleo.pl/api/v2/main/stations"

        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->
                stations = gson.fromJson(response, stationsArrayType)
                saveStationsData(response)
                updateCacheExpirationTime()
                callback.onSuccess()
            },
            Response.ErrorListener { println("REQUEST FAILED") }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-KOLEO-Version"] = "1"
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun getStationsDataFromCachedFile(callback: ApiRequestCallback) {
        stations = gson.fromJson(stationsDataFile.readText(), stationsArrayType)
        callback.onSuccess()
    }
}