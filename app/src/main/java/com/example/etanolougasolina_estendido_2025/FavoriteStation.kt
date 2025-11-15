package com.example.etanolougasolina_estendido_2025
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
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"