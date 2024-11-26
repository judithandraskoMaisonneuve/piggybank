package com.example.piggybank_projet3

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get references to inputs and buttons
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.inputConfirmPassword)
        val inputFirstname = findViewById<EditText>(R.id.inputFirstname)
        val inputLastname = findViewById<EditText>(R.id.inputLastname)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val btnGoogleSignup = findViewById<Button>(R.id.btnSignupGoogle)

        // Set up the sign-up button click listener
        btnSignup.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()
            val firstname = inputFirstname.text.toString().trim()
            val lastname = inputLastname.text.toString().trim()

            if (!isEmailValid(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else if (!arePasswordsMatching(password, confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password does not meet the requirements!", Toast.LENGTH_SHORT).show()
            } else {
                signUpWithEmail(email, password, firstname, lastname)
            }
        }

        // Set up Google Sign-In button click listener
        btnGoogleSignup.setOnClickListener {
            signInWithGoogle()
        }

        configureGoogleSignIn()
    }

    // Configure Google Sign-In
    private fun configureGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    // Google Sign-In logic
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        createUserInFirestore(user.uid, user.email ?: "", "", "")
                    }
                    navigateToDashboard()
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUpWithEmail(email: String, password: String, firstname: String, lastname: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        createUserInFirestore(user.uid, email, firstname, lastname)
                    }
                    navigateToDashboard()
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserInFirestore(userId: String, email: String, firstname: String, lastname: String) {
        val user = hashMapOf(
            "id_user" to userId,
            "lastname" to lastname,
            "firstname" to firstname,
            "email" to email,
            "language" to "English",
            "id_income" to null,
            "id_expense" to null,
            "id_budget" to null,
            "id_goal" to null
        )

        firestore.collection("Users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User data added to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun arePasswordsMatching(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return passwordPattern.matches(password)
    }

    // Navigate to DashBoardActivity
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish() // Prevent user from returning to the signup activity
    }
}
