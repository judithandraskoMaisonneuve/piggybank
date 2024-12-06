package com.example.piggybank_projet3

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseAdapter(private val expenses: MutableList<Expense>) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expenseName: TextView = view.findViewById(R.id.expense_name)
        val expenseAmount: TextView = view.findViewById(R.id.expense_amount)
        private val deleteButton: ImageButton = view.findViewById(R.id.btndelete_expense)

        fun bind(expense: Expense, onDelete: (Int) -> Unit) {
            expenseName.text = expense.name
            expenseAmount.text = String.format("$%.2f", expense.amount)
            deleteButton.setOnClickListener {
                onDelete(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.expenseName.text = expense.name
        holder.expenseAmount.text = "$${expense.amount}" // Format the amount
        holder.bind(expenses[position]) { pos ->
            // Show confirmation dialog before deleting
            showDeleteConfirmationDialog(expense, holder.itemView.context, pos)
        }
    }

    override fun getItemCount(): Int = expenses.size

    // Function to delete the expense from Firestore and local list
    private fun deleteExpense(expense: Expense, context: Context, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("Users")
            .document(userId)
            .collection("expenses")
            .document(expense.id_expense)  // Assuming Expense object has an id_expense field
            .delete()
            .addOnSuccessListener {
                // Remove from local list and update UI
                expenses.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to show a confirmation dialog before deleting an expense
    private fun showDeleteConfirmationDialog(expense: Expense, context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ ->
                deleteExpense(expense, context, position)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
