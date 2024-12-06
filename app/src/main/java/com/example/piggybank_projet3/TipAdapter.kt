package com.example.piggybank_projet3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TipAdapter(
    private val tips: List<Tip>,
    private val context: Context
) : RecyclerView.Adapter<TipAdapter.TipViewHolder>() {

    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTipTitle)
        val description: TextView = itemView.findViewById(R.id.tvTipDescription)
        val moreInfo: Button = itemView.findViewById(R.id.btnMoreInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tips, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.title.text = tip.title
        holder.description.text = tip.description

        holder.moreInfo.setOnClickListener {
            // Example external link for more information
            val url = "https://www.example.com/financial-tips"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tips.size
}

data class Tip(val title: String, val description: String)
