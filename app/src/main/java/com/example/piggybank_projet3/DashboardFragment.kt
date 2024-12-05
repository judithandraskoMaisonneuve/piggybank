package com.example.piggybank_projet3.ui.dashboard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piggybank_projet3.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val goals = mutableListOf<Goal>()
    private val expenses = mutableListOf<Expense>()
    private val incomes = mutableListOf<Income>()

    private val repository = FirestoreRepository()

    private lateinit var goalAdapter: GoalAdapter
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var incomeAdapter: IncomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Goals RecyclerView
        val rvGoals = view.findViewById<RecyclerView>(R.id.rvGoals)
        val btnAddGoal = view.findViewById<ImageButton>(R.id.btnAddGoal)

        goalAdapter = GoalAdapter(goals)
        rvGoals.layoutManager = LinearLayoutManager(requireContext())
        rvGoals.adapter = goalAdapter

        // Expenses RecyclerView
        val rvExpenses = view.findViewById<RecyclerView>(R.id.rvExpenses)
        val btnAddExpense = view.findViewById<ImageButton>(R.id.btnAddExpense)

        expenseAdapter = ExpenseAdapter(expenses)
        rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        rvExpenses.adapter = expenseAdapter

        // Incomes RecyclerView
        val rvIncomes = view.findViewById<RecyclerView>(R.id.rvIncomes)
        val btnAddIncome = view.findViewById<ImageButton>(R.id.btnAddIncome)

        incomeAdapter = IncomeAdapter(incomes)
        rvIncomes.layoutManager = LinearLayoutManager(requireContext())
        rvIncomes.adapter = incomeAdapter

        // Load data
        viewLifecycleOwner.lifecycleScope.launch {
            loadGoals()
            loadExpenses()
            loadIncomes()
        }

        // Add button actions
        btnAddGoal.setOnClickListener { showAddGoalDialog() }
        btnAddExpense.setOnClickListener { showAddExpenseDialog() }
        btnAddIncome.setOnClickListener { showAddIncomeDialog() }

        return view
    }

    private suspend fun loadGoals() {
        val newGoals = repository.getGoals()
        if (newGoals != goals) {
            goals.clear()
            goals.addAll(newGoals)
            goalAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun loadExpenses() {
        val newExpenses = repository.getExpenses()
        if (newExpenses != expenses) {
            expenses.clear()
            expenses.addAll(newExpenses)
            expenseAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun loadIncomes() {
        val newIncomes = repository.getIncomes()
        if (newIncomes != incomes) {
            incomes.clear()
            incomes.addAll(newIncomes)
            incomeAdapter.notifyDataSetChanged()
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_name_input)
        val goalDeadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline_input)
        val goalAmountInput = dialogView.findViewById<EditText>(R.id.goal_amount_input)

        // Set default date (6 months from now)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 6)  // Add 6 months
        val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        goalDeadlineInput.setText(defaultDate)

        // Date picker for goal deadline
        goalDeadlineInput.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Format the selected date as YYYY-MM-DD
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    goalDeadlineInput.setText(selectedDate)  // Set the selected date
                },
                calendar.get(Calendar.YEAR),  // Default year
                calendar.get(Calendar.MONTH),  // Default month (6 months from now)
                calendar.get(Calendar.DAY_OF_MONTH)  // Default day
            )
            datePickerDialog.show()
        }

        // Show the dialog
        AlertDialog.Builder(requireContext())
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
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
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
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.expense_category_spinner)
        val customCategoryLayout = dialogView.findViewById<View>(R.id.custom_category_input_layout)
        val customCategoryInput = dialogView.findViewById<EditText>(R.id.custom_category_input)

        // Set default date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        expenseDateInput.setText(currentDate)

        // Date picker
        expenseDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    expenseDateInput.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Set up category spinner with predefined and custom options
        val categories = listOf("Utilities", "Fun", "Food", "Transportation", "Other", "Add Custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Show custom input field if "Add Custom" is selected
                customCategoryLayout.visibility =
                    if (categories[position] == "Add Custom") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                customCategoryLayout.visibility = View.GONE
            }
        })

        // Show the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = expenseNameInput.text.toString()
                val date = expenseDateInput.text.toString()
                val amount = expenseAmountInput.text.toString().toDoubleOrNull()
                val selectedCategory = if (categorySpinner.selectedItem.toString() == "Add Custom") {
                    customCategoryInput.text.toString().takeIf { it.isNotEmpty() }
                        ?: "Other"
                } else {
                    categorySpinner.selectedItem.toString()
                }

                if (name.isNotEmpty() && date.isNotEmpty() && amount != null && selectedCategory.isNotEmpty()) {
                    val newExpense = Expense("", name, date, selectedCategory, amount)
                    saveExpenseToFirestore(newExpense)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
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
                requireContext(),
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

        AlertDialog.Builder(requireContext())
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
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGoalDetailsDialog(goal: Goal) {
        // Inflate the goal details dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_goal_details, null)

        // Bind views from the layout
        val goalNameTextView = dialogView.findViewById<TextView>(R.id.goal_name)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        val goalAmountTextView = dialogView.findViewById<TextView>(R.id.goal_amount)
        val moneySavedInput = dialogView.findViewById<EditText>(R.id.money_saved_input)
        val btnDeleteGoal = dialogView.findViewById<Button>(R.id.btn_delete_goal)

        // Set goal details in the dialog
        goalNameTextView.text = goal.name
        goalAmountTextView.text = "Total Goal: $${goal.amountNeeded}"
        moneySavedInput.setText(goal.savedAmount.toString())
        progressBar.progress = ((goal.savedAmount / goal.amountNeeded) * 100).toInt()

        // Set Delete Button Click Listener
        btnDeleteGoal.setOnClickListener {
            deleteGoal(goal)  // Call the function to delete the goal
        }

        // Show the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Goal Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save the updated saved amount
                val newSavedAmount = moneySavedInput.text.toString().toDoubleOrNull()
                if (newSavedAmount != null && newSavedAmount >= 0) {
                    val updatedGoal = goal.copy(savedAmount = newSavedAmount)
                    updateGoalInFirestore(updatedGoal)  // Call a function to update goal in Firestore
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to delete the goal from Firestore
    private fun deleteGoal(goal: Goal) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(userId)
            .collection("goals")
            .document(goal.id_goal)
            .delete()
            .addOnSuccessListener {
                // Remove from local list and update UI
                goals.remove(goal)
                goalAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Goal deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to update the goal in Firestore
    private fun updateGoalInFirestore(goal: Goal) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(userId)
            .collection("goals")
            .document(goal.id_goal)
            .set(goal)
            .addOnSuccessListener {
                // Update local list and notify adapter
                goals.find { it.id_goal == goal.id_goal }?.apply {
                    savedAmount = goal.savedAmount
                }
                goalAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Goal updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(requireContext(), "Goal added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add goal: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Income added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add income: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
