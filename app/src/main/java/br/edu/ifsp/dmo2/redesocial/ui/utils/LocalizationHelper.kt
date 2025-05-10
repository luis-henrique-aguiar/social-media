package br.edu.ifsp.dmo2.redesocial.ui.utils

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.util.Locale

class LocationHelper(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) {
    interface Callback {
        fun onLocationReceived(address: Address, latitude: Double, longitude: Double)
        fun onError(message: String)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(callback: Callback) {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()
        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    getAddress(lat, lon, callback)
                } else {
                    callback.onError("Location unavailable")
                }
            }
    }

    private fun getAddress(latitude: Double, longitude: Double, callback: Callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val geocoder = Geocoder(context, Locale.getDefault())
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        callback.onLocationReceived(addresses[0], latitude, longitude)
                    } else {
                        callback.onError("Address not found")
                    }
                }

                override fun onError(errorMessage: String?) {
                    callback.onError(errorMessage ?: "Unknown Geocoder error")
                }
            })
        } else {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    callback.onLocationReceived(addresses[0], latitude, longitude)
                } else {
                    callback.onError("Address not found")
                }
            } catch (e: IOException) {
                callback.onError("Geocoder error: ${e.message}")
            }
        }
    }
}