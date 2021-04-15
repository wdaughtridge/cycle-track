package com.will.CycleTrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class ProfileActivity : AppCompatActivity() {
    private lateinit var routeRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = "Profile"

        routeRecycler = findViewById(R.id.previousRoutes)

        doAsync {
            try {
                val sources = RouteManager().requestRoutes()

                runOnUiThread {
                    val adapter = RouteAdapter(sources, this@ProfileActivity, term)
                    routeRecycler.adapter = adapter
                    routeRecycler.layoutManager = LinearLayoutManager(this@ProfileActivity)
                }
            } catch(exception: Exception) {
                Log.e("Sources API", "Sources API req failed.", exception)
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to retrieve sources...", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}