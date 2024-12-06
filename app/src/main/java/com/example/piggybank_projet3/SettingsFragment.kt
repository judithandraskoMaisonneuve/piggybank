package com.example.piggybank_projet3

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var tvEmail: TextView
    private lateinit var btnSave: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize UI components
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        tvEmail = view.findViewById(R.id.tvEmail)
        btnSave = view.findViewById(R.id.btnSave)

        // Pre-populate fields with current user information
        populateUserInfo()

        // Set the save button listener
        btnSave.setOnClickListener {
            updateUserInfo()
        }

        return view
    }

    // This function will load the current user's data from Firestore and set it to the EditText fields.
    private fun populateUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            // Assuming user data is already saved in Firestore under the "Users" collection
            firestore.collection("Users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Set user data to EditText fields
                        etFirstName.setText(document.getString("firstname"))
                        etLastName.setText(document.getString("lastname"))
                        tvEmail.text = document.getString("email")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // This function will be called when the user clicks the "Save Modifications" button.
    private fun updateUserInfo() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()

        // Validate the input fields
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            // Update Firestore with the new user data
            val userRef = firestore.collection("Users").document(user.uid)
            val updatedData: Map<String, Any> = hashMapOf(
                "firstname" to firstName,
                "lastname" to lastName
            )

            // Update Firestore
            userRef.update(updatedData)
                .addOnSuccessListener {
                    // After successfully updating, we set the updated info in the fields
                    etFirstName.setText(firstName)
                    etLastName.setText(lastName)
                    // Keep the email the same, it's non-editable
                    Toast.makeText(requireContext(), "User data updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
