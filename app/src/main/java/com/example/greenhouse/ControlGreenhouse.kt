package com.example.greenhouse

data class ControlGreenhouse(
    var documentId: String="",
    val name: String="",
    var temperature: String="",
    var humidity: String="",
    var acidity: String="",
    var isWateringOn: Boolean = false,
    var isWindowsOpen: Boolean = false,
)
