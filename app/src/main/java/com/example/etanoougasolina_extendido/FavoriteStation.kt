package com.example.etanoougasolina_extendido
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteStation(
    val id: Int,
    val name: String,
    val ethanolPrice: Double,
    val gasolinePrice: Double,
    val efficiency: Float,
    val ratio: Double,
    val recommendation: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"