package com.gulderbone.koleo

import android.app.Activity
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.gulderbone.koleo.models.Station
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stationsDao = StationsDao(this)
        stationsDao.getStationsData(object : ApiRequestCallback {
            override fun onSuccess() {
                enableAutoComplete(stationsDao)
            }
        })

        calculateDistanceButton.setOnClickListener {
            hideKeyboard()
            showDistance(stationsDao)
        }
    }

    private fun enableAutoComplete(dao: StationsDao) {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            dao.stations.sortedByDescending { it.hits }.map { it.name })
        fromStationTextView.setAdapter(adapter)
        toStationTextView.setAdapter(adapter)
    }

    private fun showDistance(dao: StationsDao) {
        val fromStationText = fromStationInput.editText?.text.toString()
        val toStationText = toStationInput.editText?.text.toString()
        val fromStation = dao.stations.firstOrNull {
            it.name == fromStationText
        }
        val toStation = dao.stations.firstOrNull {
            it.name == toStationText
        }
        when {
            fromStation == null -> {
                distanceTextView.text = getString(R.string.startingStationNotFound)
            }
            toStation == null -> {
                distanceTextView.text = getString(R.string.destinationStationNotFound)
            }
            else -> {
                val distance = calculateDistanceBetweenStations(fromStation, toStation)
                distanceTextView.text = "${String.format("%.2f", distance)} km"
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

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}