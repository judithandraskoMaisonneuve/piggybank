package com.example.piggybank_projet3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(private val goals: List<Goal>) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalName: TextView = view.findViewById(R.id.goal_name)
        val goalAmount: TextView = view.findViewById(R.id.goal_amount)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]

        // Set the goal name
        holder.goalName.text = goal.name

        // Display the goal progress as a percentage of the total amount needed
        holder.progressBar.max = goal.amountNeeded.toInt()
        holder.progressBar.progress = goal.progress.toInt()

        // Set the goal amount text
        holder.goalAmount.text = "Total Goal: $${goal.amountNeeded.toInt()}"
    }

    override fun getItemCount(): Int {
        return goals.size
    }
}
