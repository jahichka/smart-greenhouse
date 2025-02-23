package com.example.greenhouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter(private val forecastList: List<TimeSeries>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.forecast_date)
        val temperatureText: TextView = view.findViewById(R.id.forecast_temperature)
        val conditionText: TextView = view.findViewById(R.id.forecast_condition)
        val weatherIcon: ImageView = view.findViewById(R.id.forecast_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecastList[position]

        // ✅ Convert timestamp to readable format
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(forecast.time)
        holder.dateText.text = parsedDate?.let { dateFormat.format(it) } ?: "Unknown Date"

        val temperature = forecast.data.instant.details.air_temperature
        holder.temperatureText.text = "$temperature°C"

        val rawCondition = forecast.data.next_1_hours?.summary?.symbol_code ?: "unknown"
        val formattedCondition = rawCondition.replace("_", " ").capitalize(Locale.getDefault())
        holder.conditionText.text = formattedCondition

        val iconResource = when (rawCondition) {
            "clearsky_day" -> R.drawable.sunny
            "partlycloudy_day" -> R.drawable.cloudy
            "cloudy" -> R.drawable.very_cloudy
            "rain" -> R.drawable.rain
            "snow" -> R.drawable.snow
            else -> R.drawable.cloudy
        }
        holder.weatherIcon.setImageResource(iconResource)
    }

    override fun getItemCount() = forecastList.size
}
