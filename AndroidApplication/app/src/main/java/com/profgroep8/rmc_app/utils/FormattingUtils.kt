package com.profgroep8.rmc_app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun formatDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    return dateTime.format(formatter)
}

fun formatLocation(latitude: Float, longitude: Float): String {
    return String.format("%.6f, %.6f", latitude, longitude)
}

