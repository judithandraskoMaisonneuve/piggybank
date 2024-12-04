package com.example.piggybank_projet3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get reference for the buttons
        val btnLogin = findViewById<Button>(R.id.btnlogin)
        val btnSignup = findViewById<Button>(R.id.btnsignup)

        // Set an OnClickListener on the login button
        btnLogin.setOnClickListener {
            // Start the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set an OnClickListener on the login button
        btnSignup.setOnClickListener {
            // Start the SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    }
}
