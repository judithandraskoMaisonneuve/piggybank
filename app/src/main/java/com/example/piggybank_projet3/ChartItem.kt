package com.example.piggybank_projet3

sealed class ChartItem {
    data class PieChartItem(val expenses: List<Expense>) : ChartItem()
    data class BarChartItem(val expenses: List<Expense>, val income: List<Income>) : ChartItem()
    data class WaterfallChartItem(val expenses: List<Expense>, val income: List<Income>, val goals: List<Goal>) : ChartItem()
}