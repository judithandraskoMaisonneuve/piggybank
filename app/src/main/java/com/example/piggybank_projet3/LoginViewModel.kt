package com.example.piggybank_projet3

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel(val app: Application) : AndroidViewModel(app) {
    val authToken = MutableLiveData<String?>()
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplication(), "Email et mot de passe sont requis", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    user?.let {
                        // Si la connexion réussit, obtenir le token d'identification de l'utilisateur
                        it.getIdToken(true).addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                authToken.value = tokenTask.result?.token
                            } else {
                                Toast.makeText(getApplication(), "Erreur de récupération du token", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplication(), "Échec de la connexion : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
