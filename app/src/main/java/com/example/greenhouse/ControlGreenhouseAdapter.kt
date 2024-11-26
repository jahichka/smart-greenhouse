package com.example.greenhouse

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ControlGreenhouseAdapter(
    private val context: Context,
    private val greenhouses: List<ControlGreenhouse>,
    private val onToggleSwitchChanged: (ControlGreenhouse, String, Boolean) -> Unit
) : RecyclerView.Adapter<ControlGreenhouseAdapter.GreenhouseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenhouseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.control_item_greenhouse, parent, false)
        return GreenhouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: GreenhouseViewHolder, position: Int) {
        val greenhouse = greenhouses[position]
        holder.bind(greenhouse)
    }

    override fun getItemCount(): Int = greenhouses.size

    inner class GreenhouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.control_greenhouse_card)
        private val greenhouseImage: ImageView = itemView.findViewById(R.id.greenhouse_image)
        private val greenhouseName: TextView = itemView.findViewById(R.id.greenhouse_name)
        private val temperatureText: TextView = itemView.findViewById(R.id.temperature_text)
        private val humidityText: TextView = itemView.findViewById(R.id.humidity_text)
        private val acidityText: TextView = itemView.findViewById(R.id.acidity_text)
        private val switchWatering: Switch = itemView.findViewById(R.id.switch_watering_system)
        private val switchWindows: Switch = itemView.findViewById(R.id.switch_window_control)

        fun bind(greenhouse: ControlGreenhouse) {
            greenhouseImage.setImageResource(greenhouse.imageResId)
            greenhouseName.text = greenhouse.name
            temperatureText.text = "Temperature: ${greenhouse.temperature}°C"
            humidityText.text = "Humidity: ${greenhouse.humidity}%"
            acidityText.text = "Acidity: ${greenhouse.acidity}"

            switchWatering.isChecked = greenhouse.isWateringOn
            switchWindows.isChecked = greenhouse.isWindowsOpen

            switchWatering.setOnCheckedChangeListener { _, isChecked ->
                onToggleSwitchChanged(greenhouse, "watering", isChecked)
            }

            switchWindows.setOnCheckedChangeListener { _, isChecked ->
                onToggleSwitchChanged(greenhouse, "windows", isChecked)
            }
        }
    }
}
