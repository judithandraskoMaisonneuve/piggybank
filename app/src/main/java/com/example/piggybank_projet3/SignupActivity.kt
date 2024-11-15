package com.example.piggybank_projet3

import android.os.Bundle
import android.util.Patterns
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to inputs and buttons
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.inputConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        // Animation
        val logo = findViewById<ImageView>(R.id.imgvLogoNoBG)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        logo.startAnimation(rotateAnimation)

        // Set up the sign-up button click listener
        btnSignup.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()

            if (!isEmailValid(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else if (!arePasswordsMatching(password, confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password does not meet the requirements!", Toast.LENGTH_SHORT).show()
            } else {
                signUpWithEmail(email, password)
            }
        }
    }

    // Function to check if email is valid
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

    // Firebase Sign Up
    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to login or another activity
                } else {
                    // If sign up fails, display a message to the user
                    Toast.makeText(
                        this,
                        "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
