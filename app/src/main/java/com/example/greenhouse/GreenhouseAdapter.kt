package com.example.greenhouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GreenhouseAdapter(
    private val items: List<Greenhouse>,
    private val onItemClick: (Greenhouse) -> Unit
) : RecyclerView.Adapter<GreenhouseAdapter.GreenhouseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenhouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_greenhouse, parent, false)
        return GreenhouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: GreenhouseViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class GreenhouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.greenhouse_name)
        private val humidity = itemView.findViewById<TextView>(R.id.greenhouse_humidity)
        private val temperature = itemView.findViewById<TextView>(R.id.greenhouse_temperature)
        private val acidity = itemView.findViewById<TextView>(R.id.greenhouse_soil_acidity)

        fun bind(data: Greenhouse) {
            name.text = data.name
            humidity.text = "${data.humidity}%"
            temperature.text = "${data.temperature}°C"
            acidity.text = "${data.acidity}"

            itemView.setOnClickListener {
                onItemClick(data)
            }
        }
    }
}



