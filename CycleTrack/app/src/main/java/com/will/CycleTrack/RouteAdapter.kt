package com.will.CycleTrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text


class RouteAdapter(private var routes: List<Route>, private var context: Context) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.route_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curRoute = routes[position]
        val dist = curRoute.distance.toInt().toString()

        holder.date.text = curRoute.date
        holder.routeDistance.text = "${dist}m"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RouteActivity::class.java)
            intent.putExtra ("docID", curRoute.docID)
            intent.putExtra("timestamp", curRoute.date)
            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.route_date)
        val routeDistance: TextView = itemView.findViewById(R.id.routeDistance)
    }
}