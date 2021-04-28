package com.will.CycleTrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import org.jetbrains.anko.doAsync
import org.w3c.dom.Text

class RegisterActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var password_confirm: EditText
    private lateinit var signUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()

        username = findViewById(R.id.user_register)
        password = findViewById(R.id.pass_register)
        password_confirm = findViewById(R.id.pass_confirm)
        signUp = findViewById(R.id.signUp)

        signUp.setOnClickListener {
            if (password.text.toString() == password_confirm.text.toString() &&
                        username.text.toString().isNotBlank() &&
                        password.text.toString().isNotBlank()) {
                firebaseAuth.createUserWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Creating account..", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Failed to create account..\n${task.exception}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please check the input fields", Toast.LENGTH_LONG).show()
            }
        }
    }
}