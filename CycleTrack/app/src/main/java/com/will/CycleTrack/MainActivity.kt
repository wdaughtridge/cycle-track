package com.will.CycleTrack

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class MainActivity : AppCompatActivity() {
    private lateinit var goToMap: Button
    private lateinit var username: AppCompatEditText
    private lateinit var password: AppCompatEditText
    private lateinit var register: Button
    private lateinit var rememberCreds: Switch

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (firebaseAuth.currentUser != null)
            firebaseAuth.signOut()

        goToMap = findViewById(R.id.go_to_map)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        register = findViewById(R.id.register)
        rememberCreds = findViewById(R.id.rememberCreds)

        val preferences = getSharedPreferences("cycleTrack", Context.MODE_PRIVATE)

        username.setText(preferences.getString("username", ""), TextView.BufferType.EDITABLE)
        password.setText(preferences.getString("password", ""), TextView.BufferType.EDITABLE)
        rememberCreds.isChecked = preferences.getString("rememberCreds", "") == "true"

        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        goToMap.setOnClickListener { v: View ->
            val user: String = username.text.toString()
            val pass: String = password.text.toString()

            firebaseAuth
                .signInWithEmailAndPassword(user, pass)
                .addOnCompleteListener { task: Task<AuthResult> ->

                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("login_success", null)

                        val currentUser = firebaseAuth.currentUser!!
                        val email = currentUser.email

                        preferences.edit()
                            .putString("username", email)
                            .apply()

                        if (rememberCreds.isChecked) {
                            preferences.edit()
                                .putString("password", password.text.toString())
                                .apply()

                            preferences.edit()
                                .putString("rememberCreds", "true")
                                .apply()
                        } else {
                            preferences.edit()
                                .putString("password", "")
                                .apply()

                            preferences.edit()
                                .putString("rememberCreds", "false")
                                .apply()
                        }

                        Toast.makeText(this, "Logging in now..", Toast.LENGTH_LONG).show()

                        val intent = Intent(this, MapsActivity::class.java)
                        intent.putExtra("userEmail", email)
                        startActivity(intent)
                    } else {
                        val exception = task.exception
                        val bundle = Bundle()
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            bundle.putString("error_type", "invalid_credentials")
                            firebaseAnalytics.logEvent("login_failed", bundle)
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG).show()
                        } else {
                            bundle.putString("error_type", "generic_failure")
                            firebaseAnalytics.logEvent("login_failed", bundle)
                            Toast.makeText(this, "Failed to login", Toast.LENGTH_LONG).show()
                        }
                    }

                }

        }
    }
}