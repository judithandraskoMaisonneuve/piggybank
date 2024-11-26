package com.example.piggybank_projet3

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val goals = mutableListOf<Goal>()
    private val expenses = mutableListOf<Expense>()
    private val incomes = mutableListOf<Income>()

    private lateinit var goalAdapter: GoalAdapter
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var incomeAdapter: IncomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Goals RecyclerView
        val rvGoals = findViewById<RecyclerView>(R.id.rvGoals)
        val btnAddGoal = findViewById<ImageButton>(R.id.btnAddGoal)

        goalAdapter = GoalAdapter(goals)
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = goalAdapter

        // Expenses RecyclerView
        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        val btnAddExpense = findViewById<ImageButton>(R.id.btnAddExpense)

        expenseAdapter = ExpenseAdapter(expenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = expenseAdapter

        // Incomes RecyclerView
        val rvIncomes = findViewById<RecyclerView>(R.id.rvIncomes)
        val btnAddIncome = findViewById<ImageButton>(R.id.btnAddIncome)

        incomeAdapter = IncomeAdapter(incomes)
        rvIncomes.layoutManager = LinearLayoutManager(this)
        rvIncomes.adapter = incomeAdapter

        // Load data
        loadGoalsFromFirestore()
        loadExpensesFromFirestore()
        loadIncomesFromFirestore()

        // Add button actions
        btnAddGoal.setOnClickListener { showAddGoalDialog() }
        btnAddExpense.setOnClickListener { showAddExpenseDialog() }
        btnAddIncome.setOnClickListener { showAddIncomeDialog() }
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
                    val goal = document.toObject(Goal::class.java).copy(id_goal = document.id)
                    goals.add(goal)
                }
                goalAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load goals: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpensesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(userId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                expenses.clear()
                for (document in result) {
                    val expense = document.toObject(Expense::class.java).copy(id_expense = document.id)
                    expenses.add(expense)
                }
                expenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadIncomesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(userId)
            .collection("incomes")
            .get()
            .addOnSuccessListener { result ->
                incomes.clear()
                for (document in result) {
                    val income = document.toObject(Income::class.java).copy(id_income = document.id)
                    incomes.add(income)
                }
                incomeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load incomes: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val progress = 0.0

                if (name.isNotEmpty() && deadline.isNotEmpty() && amount != null) {
                    val newGoal = Goal("", name, deadline, amount, progress)
                    saveGoalToFirestore(newGoal)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val expenseNameInput = dialogView.findViewById<EditText>(R.id.expense_name_input)
        val expenseDateInput = dialogView.findViewById<EditText>(R.id.expense_date_input)
        val expenseAmountInput = dialogView.findViewById<EditText>(R.id.expense_amount_input)

        // Set the current date as the default date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        expenseDateInput.setText(currentDate)

        // Open DatePicker when the user taps on the date input field
        expenseDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Format the selected date as yyyy-MM-dd
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    expenseDateInput.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = expenseNameInput.text.toString()
                val date = expenseDateInput.text.toString()
                val amount = expenseAmountInput.text.toString().toDoubleOrNull()

                if (name.isNotEmpty() && date.isNotEmpty() && amount != null) {
                    val newExpense = Expense("", name, date, amount)
                    saveExpenseToFirestore(newExpense)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddIncomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_income, null)
        val incomeNameInput = dialogView.findViewById<EditText>(R.id.income_name_input)
        val incomeDateInput = dialogView.findViewById<EditText>(R.id.income_date_input)
        val incomeAmountInput = dialogView.findViewById<EditText>(R.id.income_amount_input)

        // Set the current date as the default date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        incomeDateInput.setText(currentDate)

        // Open DatePicker when the user taps on the date input field
        incomeDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Format the selected date as yyyy-MM-dd
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    incomeDateInput.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Income")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = incomeNameInput.text.toString()
                val date = incomeDateInput.text.toString()
                val amount = incomeAmountInput.text.toString().toDoubleOrNull()

                if (name.isNotEmpty() && date.isNotEmpty() && amount != null) {
                    val newIncome = Income("", name, date, amount)
                    saveIncomeToFirestore(newIncome)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun saveGoalToFirestore(goal: Goal) {
        val userId = auth.currentUser?.uid ?: return

        val documentReference = firestore.collection("Users")
            .document(userId)
            .collection("goals")
            .document()

        val goalWithId = goal.copy(id_goal = documentReference.id)

        documentReference.set(goalWithId)
            .addOnSuccessListener {
                goals.add(goalWithId)
                goalAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Goal added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExpenseToFirestore(expense: Expense) {
        val userId = auth.currentUser?.uid ?: return

        val documentReference = firestore.collection("Users")
            .document(userId)
            .collection("expenses")
            .document()

        val expenseWithId = expense.copy(id_expense = documentReference.id)

        documentReference.set(expenseWithId)
            .addOnSuccessListener {
                expenses.add(expenseWithId)
                expenseAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveIncomeToFirestore(income: Income) {
        val userId = auth.currentUser?.uid ?: return

        val documentReference = firestore.collection("Users")
            .document(userId)
            .collection("incomes")
            .document()

        val incomeWithId = income.copy(id_income = documentReference.id)

        documentReference.set(incomeWithId)
            .addOnSuccessListener {
                incomes.add(incomeWithId)
                incomeAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Income added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add income: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
