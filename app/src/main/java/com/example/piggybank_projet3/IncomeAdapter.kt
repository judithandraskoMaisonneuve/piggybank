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

class IncomeAdapter(
    private val incomes: MutableList<Income>
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    // ViewHolder for encapsulating views
    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val incomeName: TextView = view.findViewById(R.id.income_name)
        private val incomeAmount: TextView = view.findViewById(R.id.income_amount)
        private val deleteButton: ImageButton = view.findViewById(R.id.btndelete_income)

        // Bind data to the view
        fun bind(income: Income, onDelete: (Int) -> Unit) {
            incomeName.text = income.name
            incomeAmount.text = String.format("$%.2f", income.amount) // Format the amount
            deleteButton.setOnClickListener {
                onDelete(adapterPosition) // Calls delete function with the position
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(incomes[position]) { pos ->
            // Show confirmation dialog before deleting
            showDeleteConfirmationDialog(incomes[pos], holder.itemView.context, pos)
        }
    }

    override fun getItemCount(): Int = incomes.size

    // Function to delete an income from Firestore and the local list
    private fun deleteIncome(income: Income, context: Context, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("Users")
            .document(userId)
            .collection("incomes")
            .document(income.id_income)  // Assuming Income object has an id_income field
            .delete()
            .addOnSuccessListener {
                // Remove from local list and update UI
                incomes.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Income deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete income: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to show a confirmation dialog before deleting an income
    private fun showDeleteConfirmationDialog(income: Income, context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Income")
            .setMessage("Are you sure you want to delete this income?")
            .setPositiveButton("Yes") { _, _ ->
                deleteIncome(income, context, position)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
