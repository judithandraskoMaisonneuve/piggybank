package com.example.piggybank_projet3

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val goals = mutableListOf<Goal>()
    private lateinit var adapter: GoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val rvGoals = findViewById<RecyclerView>(R.id.rvGoals)
        val btnAddGoal = findViewById<Button>(R.id.btnAddGoal)

        adapter = GoalAdapter(goals)
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = adapter

        loadGoalsFromFirestore()

        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun loadGoalsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(userId)
            .collection("goals")
            .get()
            .addOnSuccessListener { result ->
                goals.clear()
                for (document in result) {
                    val goal = document.toObject(Goal::class.java)
                    goals.add(goal)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load goals: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_name_input)
        val goalDeadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline_input)
        val goalAmountInput = dialogView.findViewById<EditText>(R.id.goal_amount_input)

        AlertDialog.Builder(this)
            .setTitle("Add Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = goalNameInput.text.toString()
                val deadline = goalDeadlineInput.text.toString()
                val amount = goalAmountInput.text.toString().toDoubleOrNull()

                if (name.isNotEmpty() && deadline.isNotEmpty() && amount != null) {
                    val newGoal = Goal(name, deadline, amount)
                    saveGoalToFirestore(newGoal)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveGoalToFirestore(goal: Goal) {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's goals subcollection
        val userGoalsCollection = firestore.collection("Users")
            .document(userId)
            .collection("goals")

        // Add the goal to Firestore
        userGoalsCollection.add(goal)
            .addOnSuccessListener { documentReference ->
                val goalId = documentReference.id // Get the newly created goal's ID

                // Update the user's document to include this goal ID in the id_goal array
                val userDocument = firestore.collection("Users").document(userId)
                userDocument.update("id_goal", FieldValue.arrayUnion(goalId))
                    .addOnSuccessListener {
                        goals.add(goal) // Add to local list for RecyclerView
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Goal added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update user with goal ID: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
