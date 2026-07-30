package com.uccd3223.p1_chai_boon_hong_2206806

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val records: List<HistoryRecord>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGameName: TextView = view.findViewById(R.id.tvGameName)
        val chipMode: Chip = view.findViewById(R.id.chipMode)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvGameName.text = record.gameName
        
        if (record.mode == "FUN") {
            holder.chipMode.visibility = View.GONE
        } else {
            holder.chipMode.visibility = View.VISIBLE
            holder.chipMode.text = when (record.mode) {
                "TIME_ATTACK" -> "Time Attack"
                "SCORE_ATTACK" -> "Score Attack"
                else -> record.mode
            }
        }
        
        holder.tvScore.text = if (record.mode == "SCORE_ATTACK") "Round ${record.score}" else "${record.score} pts"
        
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(record.timestamp))
    }

    override fun getItemCount() = records.size
}
