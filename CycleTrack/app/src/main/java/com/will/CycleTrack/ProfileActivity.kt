package com.will.CycleTrack

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync

class ProfileActivity : AppCompatActivity() {
    private lateinit var routeRecycler: RecyclerView
    private var fireStore = Firebase.firestore
    private lateinit var profileName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = resources.getString(R.string.view_profile)

        routeRecycler = findViewById(R.id.previousRoutes)

        profileName = findViewById(R.id.profileName)

        val preferences = getSharedPreferences("cycleTrack", Context.MODE_PRIVATE)
        profileName.text = preferences.getString("username", "no_username")

        try {
            requestRoutes()
        } catch(exception: Exception) {
            Log.e("Firebase API", "Firebase API req failed.", exception)
            runOnUiThread {
                Toast.makeText(this@ProfileActivity, "Failed to retrieve routes...", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestRoutes() {
        val routes: ArrayList<Route> = ArrayList()
        val preferences = getSharedPreferences("cycleTrack", Context.MODE_PRIVATE)

        doAsync {
            fireStore.collection("routes")
                .whereEqualTo("user", preferences.getString("username", "no_username"))
                .orderBy("period", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val rt = Route()

                        rt.docID = document.id
                        rt.date = document.data["timestamp"] as String
                        rt.distance = document.data["distance"] as Double

                        routes.add(rt)
                    }

                    runOnUiThread {
                        val adapter = RouteAdapter(routes, this@ProfileActivity)
                        routeRecycler.adapter = adapter
                        routeRecycler.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firebase", "Error getting documents: ", exception)
                }
        }
    }

}