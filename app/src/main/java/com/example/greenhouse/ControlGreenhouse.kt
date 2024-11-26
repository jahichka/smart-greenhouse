package com.example.greenhouse

data class ControlGreenhouse(
    val name: String,
    val temperature: String,
    val humidity: String,
    val acidity: String,
    val imageResId: Int,
    var isWateringOn: Boolean = false,
    var isWindowsOpen: Boolean = false
)
