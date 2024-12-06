package com.example.piggybank_projet3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class BudgetFragment : Fragment() {
    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        // Initialize RecyclerView
        val budgetRecyclerView: RecyclerView = view.findViewById(R.id.rvBudget)
        budgetRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load data asynchronously
        lifecycleScope.launch {
            val expenses = repository.getExpenses().sumOf { it.amount }
            val income = repository.getIncomes().sumOf { it.amount }

            // Prepare BudgetItem list
            val budgetItems = listOf(
                Budget(
                    title = "Current Budget",
                    expenses = expenses,
                    income = income
                )
            )

            // Set up adapter
            val budgetAdapter = BudgetAdapter(budgetItems, requireContext())
            budgetRecyclerView.adapter = budgetAdapter
        }


        return view
    }
}
