package com.will.CycleTrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.US
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var polyLine: PolylineOptions
    private lateinit var startRecording: Button
    private lateinit var viewProfile: Button

    private var curLoc: LatLng? = null
    private var distOfRoute: Double = 0.0
    private var addToPolyline = false
    private var curRoute = ArrayList<LatLng>()
    private var fireStore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewProfile = findViewById(R.id.viewProfile)
        viewProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        startRecording = findViewById(R.id.startRecording)
        startRecording.setOnClickListener {
            if (startRecording.text.toString() == resources.getString(R.string.start_recording)) {
                curRoute = ArrayList()
                distOfRoute = 0.0
                polyLine = PolylineOptions().clickable(true)
                addToPolyline = true

                startRecording.text = resources.getString(R.string.stop_recording)
            } else {
                val listOfLatLngs = curRoute.toList()
                polyLine = PolylineOptions().clickable(true)
                addToPolyline = false

                startRecording.text = resources.getString(R.string.start_recording)

                val preferences = getSharedPreferences("cycleTrack", Context.MODE_PRIVATE)

                doAsync {
                    fireStore.collection("routes").add(
                            hashMapOf(
                                "user" to preferences.getString("username", "no_username"),
                                "timestamp" to SimpleDateFormat("hh:mm:ss a MM/dd/yyyy", US).format(Date()),
                                "period" to SimpleDateFormat("yyyyMMddHHmmss", ).format(Date()),
                                "distance" to distOfRoute,
                                "points" to listOfLatLngs)
                        )
                        .addOnSuccessListener { documentReference ->
                            Log.d("Firebase", "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firebase", "Error adding document", e)
                        }
                }
            }
        }

        polyLine = PolylineOptions().clickable(true)

        title = "CycleTrack"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    val latLng = LatLng(location.latitude, location.longitude)

                    distOfRoute += if (curLoc != null) computeDist(curLoc!!, latLng) else 0.0

                    curLoc = latLng
                    polyLine.add(latLng)
                    curRoute.add(latLng)

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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f))

        if (addToPolyline)
            mMap.addPolyline(polyLine)
    }

    private fun computeDist(pt1: LatLng, pt2: LatLng): Double {
        // citation: https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
        // had no idea how to do this without looking up formula
        // I still do not know if the distances I am getting are accurate
        val deltaLatRad = (pt2.latitude - pt1.latitude) * (Math.PI/180.0)
        val deltaLonRad = (pt2.longitude - pt1.longitude) * (Math.PI/180.0)
        val a = sin(deltaLatRad/2.0) * sin(deltaLatRad/2.0) + cos(pt1.latitude * (Math.PI/180.0)) * cos(pt2.latitude * (Math.PI/180.0)) * sin(deltaLonRad/2.0) * sin(deltaLonRad/2.0)
        val c = 2.0 * atan2(Math.sqrt(a), Math.sqrt(1-a))
        return 6371 * 1000 * c
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        curLoc = null
        distOfRoute = 0.0

        val initLoc = LatLng(0.0, 0.0)
        moveArrow(mMap, R.drawable.bicycle, 120, 120, initLoc)

        mMap.setOnMapLongClickListener { coords: LatLng ->
            mMap.clear()

            doAsync {
                val geoCoder = Geocoder(this@MapsActivity)

                try {
                    geoCoder.getFromLocation(coords.latitude, coords.longitude, 10)
                } catch (e: Exception) { Log.e("MapsActivity", "Geocoder failed", e); listOf() } // trying to condense the length of this monster fn for time being

                runOnUiThread {
                    moveArrow(mMap, R.drawable.bicycle, 120, 120, coords)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    200)
        } else {
            val locationRequest = LocationRequest.create().apply {
                interval = 500
                fastestInterval = 250
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}