package com.example.piggybank_projet3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import kotlinx.coroutines.launch

class AnalysisFragment : Fragment() {

    private val repository = FirestoreRepository()

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val expenses = repository.getExpenses()

            // Prepare the expense categories data
            val categoryData = mutableMapOf<String, Double>()

            // Categorize expenses
            expenses.forEach { expense ->
                categoryData[expense.category] = categoryData.getOrDefault(expense.category, 0.0) + expense.amount
            }

            // Prepare the PieChart data
            val pieEntries = categoryData.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            // Update the PieChart in the UI
            updatePieChart(pieEntries)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        // Initialize the PieChart
        pieChart = view.findViewById(R.id.pieChart)

        return view
    }

    // Function to update the PieChart
    private fun updatePieChart(entries: List<PieEntry>) {
        val pieDataSet = PieDataSet(entries, " Expense Categories")
        pieDataSet.setColors(*com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS)  // Set colors
        pieDataSet.valueTextColor = android.graphics.Color.BLACK
        pieDataSet.valueTextSize = 14f

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.invalidate() // Refresh the chart
    }
}
