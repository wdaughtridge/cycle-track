package com.will.CycleTrack

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.GeomagneticField
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.jetbrains.anko.doAsync
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var polyLine: PolylineOptions

    private var curLoc: LatLng? = null
    private var distOfRoute: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        polyLine = PolylineOptions().clickable(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    val latLng = LatLng(location.latitude, location.longitude)

                    if (curLoc != null) distOfRoute += computeDist(curLoc!!, latLng) else distOfRoute += computeDist(latLng, latLng)
                    Log.d("distance", "$distOfRoute")

                    curLoc = latLng
                    polyLine.add(latLng)

                    runOnUiThread {
                        moveArrow(mMap, R.drawable.bicycle, 120, 120, latLng)
                    }
                }
            }
        }

        startLocationUpdates()
    }

    // scale the nav icon, draw, and move camera
    private fun moveArrow(mMap: GoogleMap, iconID: Int, width: Int, height: Int, latLng: LatLng) {
        mMap.clear()

        val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, iconID), width, height, false)
        mMap.addMarker(MarkerOptions().position(latLng).title("Current location").icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap)).anchor(0.5f, 0.5f))

        /*.rotation(
            GeomagneticField(latLng.latitude.toFloat(), latLng.longitude.toFloat(), 100.0f, 500L).declination - 45.0f)
        )*/

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.addPolyline(polyLine)
    }

    private fun computeDist(pt1: LatLng, pt2: LatLng): Double {
        // citation: https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
        // had no idea how to do this without looking up formula
        val deltaLatRad = (pt2.latitude - pt1.latitude) * (Math.PI/180.0)
        val deltaLonRad = (pt2.longitude - pt1.longitude) * (Math.PI/180.0)
        val a = sin(deltaLatRad/2.0) * sin(deltaLatRad/2.0) + cos(pt1.latitude * (Math.PI/180.0)) * cos(pt2.latitude * (Math.PI/180.0)) * sin(deltaLonRad/2.0) * sin(deltaLonRad/2.0)
        val c = 2.0 * atan2(Math.sqrt(a), Math.sqrt(1-a))
        return 6371.0 * c
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        curLoc = null
        distOfRoute = 0.0

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    200);
        }

        val initLoc = LatLng(0.0, 0.0)
        mMap.addMarker(MarkerOptions().position(initLoc).title("Marker at current location").icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initLoc))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f))

        mMap.setOnMapLongClickListener { coords: LatLng ->
            mMap.clear()

            doAsync {
                val geoCoder = Geocoder(this@MapsActivity)

                val results: List<Address> = try {
                    geoCoder.getFromLocation(coords.latitude, coords.longitude, 10)
                } catch (e: Exception) { Log.e("MapsActivity", "Geocoder failed", e); listOf() } // trying to condense the length of this monster fn for time being

                runOnUiThread {
                    mMap.addMarker(MarkerOptions().position(coords).title(results.first().countryName))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(coords))
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    200);
        }

        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}