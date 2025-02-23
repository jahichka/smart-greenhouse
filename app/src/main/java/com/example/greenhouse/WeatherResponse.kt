package com.example.greenhouse

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val properties: Properties
)

data class Properties(
    val timeseries: List<TimeSeries>
)

data class TimeSeries(
    val time: String,
    val data: Data
)

data class Data(
    val instant: InstantData,
    val next_1_hours: NextHourData?
)

data class InstantData(
    val details: WeatherDetails
)

data class NextHourData(
    val summary: WeatherSummary
)

data class WeatherDetails(
    val air_temperature: Double,
    val precipitation_amount: Double?
)

data class WeatherSummary(
    val symbol_code: String
)
