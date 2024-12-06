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
        holder.progressBar.progress = percentage

        // Change progress bar color based on percentage
        when {
            percentage <= 70 -> holder.progressBar.progressDrawable =
                ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_green)
            percentage in 71..90 -> holder.progressBar.progressDrawable =
                ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_orange)
            else -> holder.progressBar.progressDrawable =
                ContextCompat.getDrawable(context, R.drawable.progress_bar_expenses_red)
        }
    }

    override fun getItemCount(): Int = budgetItems.size
}
