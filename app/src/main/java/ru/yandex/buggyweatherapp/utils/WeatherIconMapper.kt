package ru.yandex.buggyweatherapp.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherIconMapper {

    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        return dateFormat.get()?.format(date) ?: ""
    }

    fun getWeatherDescription(description: String, temperature: Double): String {
        return buildString {
            append(description.replaceFirstChar { it.uppercase() })
            append(", ")
            append("${temperature.toInt()}°C")
        }
    }

    fun getWeatherIconResource(iconCode: String): String {
        return when (iconCode) {
            "01d" -> "☀️"
            "01n" -> "🌙"
            "02d", "02n" -> "⛅"
            "03d", "03n" -> "☁️"
            "04d", "04n" -> "☁️"
            "09d", "09n" -> "🌧️"
            "10d" -> "🌦️"
            "10n" -> "🌧️"
            "11d", "11n" -> "⛈️"
            "13d", "13n" -> "❄️"
            "50d", "50n" -> "🌫️"
            else -> "🌤️"
        }
    }

    fun getBackgroundColor(weatherId: Int, temperature: Double): Color {
        return when {
            weatherId in 200..299 -> Color(0xFF4A5568)
            weatherId in 300..399 -> Color(0xFF718096)
            weatherId in 500..599 -> Color(0xFF4299E1)
            weatherId in 600..699 -> Color(0xFFBEE3F8)
            weatherId in 700..799 -> Color(0xFFCBD5E0)
            weatherId == 800 -> {
                when {
                    temperature > 30 -> Color(0xFFFF6B6B)
                    temperature > 20 -> Color(0xFFFFD93D)
                    temperature > 10 -> Color(0xFF6BCB77)
                    temperature > 0 -> Color(0xFF4ECDC4)
                    else -> Color(0xFF95E1D3)
                }
            }
            weatherId in 801..804 -> Color(0xFFA0AEC0)
            else -> Color(0xFFEDF2F7)
        }
    }
}
