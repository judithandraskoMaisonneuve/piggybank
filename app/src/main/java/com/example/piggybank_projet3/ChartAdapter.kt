package com.example.piggybank_projet3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartAdapter(private val chartItems: List<ChartItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PIE_CHART = 0
        const val BAR_CHART = 1
        const val WATERFALL_CHART = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (chartItems[position]) {
            is ChartItem.PieChartItem -> PIE_CHART
            is ChartItem.BarChartItem -> BAR_CHART
            is ChartItem.WaterfallChartItem -> WATERFALL_CHART
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PIE_CHART -> {
                val view = inflater.inflate(R.layout.item_pie_chart, parent, false)
                PieChartViewHolder(view)
            }
            BAR_CHART -> {
                val view = inflater.inflate(R.layout.item_bar_chart, parent, false)
                BarChartViewHolder(view)
            }
            WATERFALL_CHART -> {
                val view = inflater.inflate(R.layout.item_waterfall_chart, parent, false)
                WaterfallChartViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = chartItems[position]) {
            is ChartItem.PieChartItem -> (holder as PieChartViewHolder).bind(item)
            is ChartItem.BarChartItem -> (holder as BarChartViewHolder).bind(item)
            is ChartItem.WaterfallChartItem -> (holder as WaterfallChartViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = chartItems.size

    // ViewHolder for PieChart
    class PieChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pieChart: PieChart = itemView.findViewById(R.id.pieChart)
        private val titleTextView: TextView = itemView.findViewById(R.id.titlepiechart)

        fun bind(item: ChartItem.PieChartItem) {
            titleTextView.text = "Expense Categories Overview"
            // Bind data to PieChart
            val categoryData = item.expenses.groupBy { it.category }.mapValues { entry ->
                entry.value.sumOf { it.amount }
            }
            val pieEntries = categoryData.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }
            val pieDataSet = PieDataSet(pieEntries, "Expense Categories").apply {
                colors = listOf(
                    ContextCompat.getColor(itemView.context, R.color.pastelpink),
                    ContextCompat.getColor(itemView.context, R.color.pastelorange),
                    ContextCompat.getColor(itemView.context, R.color.pastelyellow),
                    ContextCompat.getColor(itemView.context, R.color.pastelgreen),
                    ContextCompat.getColor(itemView.context, R.color.pastelblue)
                )
            }
            pieChart.data = PieData(pieDataSet)
            pieChart.invalidate()
        }
    }

    // ViewHolder for BarChart
    class BarChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val barChart: BarChart = itemView.findViewById(R.id.barChart)
        private val titleTextView: TextView = itemView.findViewById(R.id.barcharttitle)

        fun bind(item: ChartItem.BarChartItem) {
            titleTextView.text = "Budget Overview"
            // Group expenses and income by month
            val incomeMap = item.income.groupBy { it.date.substring(0, 7) }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            val expenseMap = item.expenses.groupBy { it.date.substring(0, 7) }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            // Get sorted list of months
            val months = (incomeMap.keys + expenseMap.keys).distinct().sorted()

            // Create BarEntries for income and expenses
            val incomeEntries = months.mapIndexed { index: Int, month: String ->
                BarEntry(index.toFloat(), incomeMap[month]?.toFloat() ?: 0f)
            }
            val expenseEntries = months.mapIndexed { index: Int, month: String ->
                BarEntry(index.toFloat(), expenseMap[month]?.toFloat() ?: 0f)
            }

            // Configure datasets
            val incomeDataSet = BarDataSet(incomeEntries, "Income").apply {
                color = itemView.context.getColor(R.color.pastelgreen)
            }
            val expenseDataSet = BarDataSet(expenseEntries, "Expenses").apply {
                color = itemView.context.getColor(R.color.pastelpink)
            }

            // Combine datasets into BarData
            val barData = BarData(incomeDataSet, expenseDataSet).apply {
                barWidth = 0.3f
            }

            // Configure BarChart
            barChart.data = barData
            barChart.groupBars(0f, 0.2f, 0.02f) // Ensure grouped bars
            barChart.xAxis.apply {
                granularity = 1f
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(months)
            }
            barChart.axisLeft.axisMinimum = 0f
            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false

            barChart.invalidate() // Refresh the chart
        }
    }


    // ViewHolder for WaterfallChart
    class WaterfallChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val barChart: BarChart = itemView.findViewById(R.id.waterfallChart)
        private val titleTextView: TextView = itemView.findViewById(R.id.waterfallcharttittle)

        fun bind(item: ChartItem.WaterfallChartItem) {
            titleTextView.text = "Income Allocation Breakdown"
            // Total income
            val totalIncome = item.income.sumOf { it.amount }

            // Total expenses
            val totalExpenses = item.expenses.sumOf { it.amount }

            // Total savings (from goal progress)
            val totalSavings = item.goals.sumOf { it.progress }

            // Remaining income (if any)
            val remainingIncome = totalIncome - totalExpenses - totalSavings

            // Create waterfall entries
            val entries = listOf(
                BarEntry(0f, totalIncome.toFloat()).apply { data = "Total Income" },
                BarEntry(1f, -totalExpenses.toFloat()).apply { data = "Expenses" },
                BarEntry(2f, -totalSavings.toFloat()).apply { data = "Savings" },
                BarEntry(3f, remainingIncome.toFloat()).apply { data = "Remaining Income" }
            )

            // Set up dataset with custom colors for each bar
            val barDataSet = BarDataSet(entries, "Income Allocation").apply {
                colors = listOf(
                    itemView.context.getColor(R.color.pastelblue), // Total Income
                    itemView.context.getColor(R.color.pastelpink), // Expenses
                    itemView.context.getColor(R.color.pastelgreen), // Savings
                    itemView.context.getColor(R.color.pastelorange) // Remaining Income
                )
                valueTextSize = 12f
            }

            // Configure BarData
            val barData = BarData(barDataSet).apply {
                barWidth = 0.5f
            }

            // Configure BarChart
            barChart.data = barData
            barChart.xAxis.apply {
                granularity = 1f
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(
                    listOf("Total Income", "Expenses", "Savings", "Remaining Income")
                )
            }
            barChart.axisLeft.axisMinimum = -totalIncome.toFloat() // Allow negative bars
            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.setFitBars(true)

            // Refresh chart
            barChart.invalidate()
        }
    }

}
