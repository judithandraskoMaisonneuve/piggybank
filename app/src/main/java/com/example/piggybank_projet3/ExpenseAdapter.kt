package com.example.piggybank_projet3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
            deleteExpense(pos)
        }
    }

    override fun getItemCount(): Int = expenses.size


    private fun deleteExpense(position: Int) {
        expenses.removeAt(position)
        notifyItemRemoved(position)
    }
}
