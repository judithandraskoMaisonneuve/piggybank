package com.example.piggybank_projet3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(private val incomes: List<Income>) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val incomeName: TextView = view.findViewById(R.id.income_name)
        val incomeAmount: TextView = view.findViewById(R.id.income_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]
        holder.incomeName.text = income.name
        holder.incomeAmount.text = "$${income.amount}"
    }

    override fun getItemCount(): Int {
        return incomes.size
    }
}
