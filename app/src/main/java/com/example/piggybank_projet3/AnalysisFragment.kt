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

class AnalysisFragment : Fragment() {

    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Load data and set up RecyclerView adapter
        lifecycleScope.launch {
            val expenses = repository.getExpenses()
            val income = repository.getIncomes()
            val goals = repository.getGoals()

            val chartItems = listOf(
                ChartItem.PieChartItem(expenses),
                ChartItem.BarChartItem(expenses, income),
                ChartItem.WaterfallChartItem(expenses, income, goals)
            )

            recyclerView.adapter = ChartAdapter(chartItems)
        }

        return view
    }
}
