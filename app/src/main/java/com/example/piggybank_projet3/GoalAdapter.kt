package com.example.piggybank_projet3

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalAdapter(private val goals: MutableList<Goal>) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    // ViewHolder that holds references to the goal view elements
    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalName: TextView = view.findViewById(R.id.goal_name)
        val goalAmount: TextView = view.findViewById(R.id.goal_amount)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val detailsButton: ImageButton = itemView.findViewById(R.id.btndetails_goal)
    }

    // Called when a new ViewHolder is created to bind the goal layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    // Called when binding data to an existing ViewHolder
    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]

        // Set the goal name
        holder.goalName.text = goal.name

        // Display the goal progress as a percentage of the total amount needed
        holder.progressBar.max = goal.amountNeeded.toInt()
        holder.progressBar.progress = ((goal.savedAmount / goal.amountNeeded) * 100).toInt()

        // Set the goal amount text
        holder.goalAmount.text = "Total Goal: $${goal.amountNeeded.toInt()}"

        // Handle the details button click to show the dialog
        holder.detailsButton.setOnClickListener {
            // Show the goal details dialog
            showGoalDetailsDialog(goal, holder.itemView.context, position)
        }
    }

    // Returns the size of the goal list
    override fun getItemCount(): Int = goals.size

    // Function to delete the goal from Firestore and the list
    private fun deleteGoal(goal: Goal, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("Users")
            .document(userId)
            .collection("goals")
            .document(goal.id_goal)
            .delete()
            .addOnSuccessListener {
                // Remove from local list and update UI
                goals.remove(goal)
                notifyDataSetChanged()
                Toast.makeText(context, "Goal deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to update the goal in Firestore
    private fun updateGoalInFirestore(goal: Goal, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("Users")
            .document(userId)
            .collection("goals")
            .document(goal.id_goal)
            .set(goal)
            .addOnSuccessListener {
                // Update the local list and notify the adapter
                goals.find { it.id_goal == goal.id_goal }?.apply {
                    savedAmount = goal.savedAmount
                }
                // Notify that specific item in the RecyclerView is updated
                notifyItemChanged(goals.indexOf(goal))
                Toast.makeText(context, "Goal updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to show the goal details in a dialog
    private fun showGoalDetailsDialog(goal: Goal, context: Context, position: Int) {
        // Inflate the goal details dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_goal_details, null)

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
            deleteGoal(goal, context)  // Call the function to delete the goal
        }

        // Show the dialog
        AlertDialog.Builder(context)
            .setTitle("Goal Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save the updated saved amount
                val newSavedAmount = moneySavedInput.text.toString().toDoubleOrNull()
                if (newSavedAmount != null && newSavedAmount >= 0) {
                    val updatedGoal = goal.copy(savedAmount = newSavedAmount)
                    updateGoalInFirestore(updatedGoal, context)  // Call a function to update goal in Firestore

                    // Update the progress bar in the dialog
                    progressBar.progress = ((newSavedAmount / goal.amountNeeded) * 100).toInt()

                    // Update the progress bar in the RecyclerView
                    val updatedGoalIndex = goals.indexOf(goal)
                    if (updatedGoalIndex != -1) {
                        // Update the goal in the list and notify the RecyclerView
                        goals[updatedGoalIndex] = updatedGoal
                        notifyItemChanged(updatedGoalIndex)
                    }

                } else {
                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
