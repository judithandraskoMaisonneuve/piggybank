package com.example.piggybank_projet3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BudgetAdapter(
    private val budgetItems: List<Budget>,
    private val context: Context
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTittleBudget)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarBudget)
        val totalIncome: TextView = itemView.findViewById(R.id.tvTotalIncome)
        val description: TextView = itemView.findViewById(R.id.description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_progress, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budgetItem = budgetItems[position]
        val percentage = if (budgetItem.income > 0) {
            (budgetItem.expenses / budgetItem.income * 100).toInt()
        } else 0

        holder.title.text = budgetItem.title
        holder.totalIncome.text = "$${budgetItem.income}" // Use string templates
        holder.progressBar.progress = percentage

        // Change progress bar color based on percentage
        when {
            percentage <= 60 -> {
                holder.progressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_green)
                holder.description.text = "You're in the green! \nYou have spent 60% or less of your income."
            }
            percentage in 61..80 -> {
                holder.progressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_orange)
                holder.description.text = "Careful! \nYou have spent more than 60% of your income."
            }
            else -> {
                holder.progressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_red)
                holder.description.text = "Woah there! \nYou have spent more than 80% of your income."
            }
        }
    }

    override fun getItemCount(): Int = budgetItems.size
}
