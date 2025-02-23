package com.example.greenhouse

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WeatherService {
    @Headers("User-Agent: mina/1.0 amina.jahic@fet.ba")
    @GET("locationforecast/2.0/compact")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponse
}
