package com.example.piggybank_projet3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(
    private val incomes: MutableList<Income>
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    // ViewHolder pour encapsuler les vues
    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val incomeName: TextView = view.findViewById(R.id.income_name)
        private val incomeAmount: TextView = view.findViewById(R.id.income_amount)
        private val deleteButton: ImageButton = view.findViewById(R.id.btndelete_income)

        // Liaison des données à la vue
        fun bind(income: Income, onDelete: (Int) -> Unit) {
            incomeName.text = income.name
            incomeAmount.text = String.format("$%.2f", income.amount) // Formatage du montant
            deleteButton.setOnClickListener {
                onDelete(adapterPosition) // Appelle la fonction de suppression avec la position
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(incomes[position]) { pos ->
            deleteIncome(pos)
        }
    }

    override fun getItemCount(): Int = incomes.size

    // Méthode pour supprimer un élément de la liste
    private fun deleteIncome(position: Int) {
        incomes.removeAt(position) // Retire l'élément de la liste
        notifyItemRemoved(position) // Met à jour la RecyclerView
    }
}
