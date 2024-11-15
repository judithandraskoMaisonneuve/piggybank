package com.example.piggybank_projet3

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Get references to inputs and buttons
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.inputConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        // Animation
        val logo = findViewById<ImageView>(R.id.imgvLogoNoBG)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        logo.startAnimation(rotateAnimation)

        // Set up the sign-up button click listener
        btnSignup.setOnClickListener {
            val password = inputPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()

            if (!arePasswordsMatching(password, confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password does not meet the requirements!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                // Proceed with signup logic here
            }
        }
    }

    // Function to check if passwords match
    private fun arePasswordsMatching(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    // Function to check if password meets the requirements
    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return passwordPattern.matches(password)
    }
}
