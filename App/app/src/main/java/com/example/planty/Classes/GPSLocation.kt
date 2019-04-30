package com.example.planty.Classes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class GPSLocation {
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps : Location? = null
    private var locationNetwork : Location? = null
    private lateinit var locationManager: LocationManager
    private lateinit var currentLatLng: LatLng

    /**Returns GPS location of device
     * @param locManager
     * @return LatLng (GPS) of device
     */
    @SuppressLint("MissingPermission")
    fun getLocation(locManager: LocationManager): LatLng { //Returns the user GPS location
        locationManager = locManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork){
            if (hasGps) {
                Log.d("MapsActivity", "HasGPS")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null){
                            locationGps = location

                        }
                    }
                    override fun onProviderDisabled(provider: String?) {}
                    override fun onProviderEnabled(provider: String?) {}
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                })
                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null){
                    locationGps = localGpsLocation
                }
            }
            if (hasNetwork) {
                Log.d("MapsActivity", "HasNetwork")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0F, object :
                    LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null){
                            locationNetwork = location

                        }
                    }
                    override fun onProviderDisabled(provider: String?) {}
                    override fun onProviderEnabled(provider: String?) {}
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                })
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null){
                    locationNetwork = localNetworkLocation
                }
            }
            if (locationNetwork != null){
                currentLatLng = LatLng(locationNetwork!!.latitude, locationNetwork!!.longitude)
                Log.d("MapsActivity", "Network populated ${currentLatLng}")
                return currentLatLng
            }
            if (locationGps != null){
                currentLatLng = LatLng(locationGps!!.latitude, locationGps!!.longitude)
                Log.d("MapsActivity", "GPS populated ${currentLatLng}")
                return currentLatLng
            }
        }
        return currentLatLng
    }
}