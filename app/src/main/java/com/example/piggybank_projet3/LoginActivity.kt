package com.example.piggybank_projet3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialiser FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.inputEmail)
        val passwordInput = findViewById<EditText>(R.id.inputPassword)
        val loginButton = findViewById<Button>(R.id.btnlogin)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Authentifier l'utilisateur avec Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Connexion réussie, naviguer vers MainActivity
                            val user = auth.currentUser
                            if (user != null) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // Échec de la connexion
                            Toast.makeText(this, "Connexion échouée, veuillez réessayer", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Veuillez saisir un email et un mot de passe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
