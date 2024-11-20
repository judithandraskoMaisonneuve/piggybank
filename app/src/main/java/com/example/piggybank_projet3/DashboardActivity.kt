package com.example.piggybank_projet3

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Expense(val name: String, val amount: String)

class DashboardActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val goals = mutableListOf<Goal>()
    private lateinit var adapter: GoalAdapter
    private lateinit var rvExpenses: RecyclerView
    private lateinit var btnAddExpense: MaterialButton
    private val expenses = mutableListOf<Expense>()
    private lateinit var expenseAdapter: ExpenseAdapter

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val rvGoals = findViewById<RecyclerView>(R.id.rvGoals)
        val btnAddGoal = findViewById<Button>(R.id.btnAddGoal)

        adapter = GoalAdapter(goals)
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = adapter

        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        rvExpenses = findViewById(R.id.rvExpenses)
        btnAddExpense = findViewById(R.id.btnAddExpense)

        expenseAdapter = ExpenseAdapter(expenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = expenseAdapter

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("expenses")

        // Load existing expenses from Firebase
        loadExpensesFromFirebase()

        btnAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

    }

    private fun loadExpensesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenses.clear() // Clear current list to avoid duplicates
                for (expenseSnapshot in snapshot.children) {
                    val expense = expenseSnapshot.getValue(Expense::class.java)
                    if (expense != null) {
                        expenses.add(expense)
                    }
                }
                expenseAdapter.notifyDataSetChanged() // Notify adapter of data change
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val expenseNameInput = dialogView.findViewById<EditText>(R.id.etExpenseName)
        val expenseAmountInput = dialogView.findViewById<EditText>(R.id.etExpenseAmount)

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = expenseNameInput.text.toString().trim()
                val amount = expenseAmountInput.text.toString().trim()

                if (name.isNotEmpty() && amount.isNotEmpty()) {
                    val formattedAmount = "$amount $ CAD"
                    val expense = Expense(name, formattedAmount)

                    // Save expense to local list
                    expenses.add(expense)
                    expenseAdapter.notifyDataSetChanged()

                    // Save expense to Firebase
                    saveExpenseToFirebase(expense)
                } else {
                    Toast.makeText(this, "Both fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun saveExpenseToFirebase(expense: Expense) {
        val expenseId = database.push().key // Generate a unique key
        if (expenseId != null) {
            database.child(expenseId).setValue(expense)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense saved to Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
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
