package com.will.CycleTrack

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(private val sources: List<Source>, private val context: Context, private val query: String) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.source_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curSrc = sources[position]

        holder.source.text = curSrc.source
        holder.content.text = curSrc.content
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ResultsActivity::class.java)
            intent.putExtra("QUERY", query)
            intent.putExtra("ID", curSrc.id)
            intent.putExtra("NAME", curSrc.source)
            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val source: TextView = itemView.findViewById(R.id.source)
        val content: TextView = itemView.findViewById(R.id.content)
    }
}