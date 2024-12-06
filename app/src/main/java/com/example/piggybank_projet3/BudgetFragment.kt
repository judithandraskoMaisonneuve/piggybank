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

        // Initialize RecyclerViews
        val budgetRecyclerView: RecyclerView = view.findViewById(R.id.rvBudget)
        budgetRecyclerView.layoutManager = LinearLayoutManager(context)

        val tipsRecyclerView: RecyclerView = view.findViewById(R.id.rvTips)
        tipsRecyclerView.layoutManager = LinearLayoutManager(context)

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

            // Set up budget adapter
            val budgetAdapter = BudgetAdapter(budgetItems, requireContext())
            budgetRecyclerView.adapter = budgetAdapter

            // Create a list of financial tips
            val financialTips = listOf(
                Tip(
                    title = "Track Your Expenses",
                    description = "Keep a close eye on where your money is going to identify areas to save.",
                    url = "https://www.canada.ca/en/financial-consumer-agency/services/covid-19-managing-financial-health.html"
                ),
                Tip(
                    title = "Set Realistic Goals",
                    description = "Start with small, achievable financial goals and gradually work your way up.",
                    url = "https://www.canada.ca/en/financial-consumer-agency/services/covid-19-managing-financial-health.html"
                ),
                Tip(
                    title = "Emergency Fund",
                    description = "Having an emergency fund can help you manage unforeseen expenses.",
                    url = "https://www.canada.ca/en/financial-consumer-agency/services/covid-19-managing-financial-health.html"
                ),
                Tip(
                    title = "Cut Unnecessary Spending",
                    description = "Review your monthly expenses and cut back on non-essential items.",
                    url = "https://www.canada.ca/en/financial-consumer-agency/services/covid-19-managing-financial-health.html"
                )
            )

            // Set up Tip adapter
            val tipAdapter = TipAdapter(financialTips, requireContext())
            tipsRecyclerView.adapter = tipAdapter
        }

        return view
    }
}
