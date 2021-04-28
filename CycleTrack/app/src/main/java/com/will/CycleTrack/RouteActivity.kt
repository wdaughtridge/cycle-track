package com.will.CycleTrack

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync

class RouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var fireStore = Firebase.firestore
    private lateinit var route: ArrayList<Map<String, Double>>
    private val polyLine = PolylineOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val id = intent.getStringExtra("docID")
        title = intent.getStringExtra("timestamp")

        if (id != null) {
            doAsync {
                fireStore.collection("routes")
                    .document(id)
                    .get()
                    .addOnSuccessListener { document ->
                        route = document.data?.get("points") as ArrayList<Map<String, Double>>

                        route.forEach {
                            polyLine.add(LatLng(it["latitude"]!!, it["longitude"]!!))
                        }

                        runOnUiThread {
                            mMap.clear()
                            mMap.addPolyline(polyLine)

                            val coord1 = LatLng(route.first()["latitude"]!!, route.first()["longitude"]!!)
                            val coord2 = LatLng(route.last()["latitude"]!!, route.last()["longitude"]!!)
                            val bounds = LatLngBounds.builder().include(coord1).include(coord2).build()

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 15.0f))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Firebase", "Error getting documents: ", exception)
                    }
            }
        }
    }
}