package com.gulderbone.koleo.models

data class Station(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val hits: Int
)