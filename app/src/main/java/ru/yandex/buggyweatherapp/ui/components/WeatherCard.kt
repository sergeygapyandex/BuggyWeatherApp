package ru.yandex.buggyweatherapp.ui.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.utils.ImageLoader
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper

@Composable
fun DetailedWeatherCard(weather: WeatherData) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imageView = remember { ImageView(context) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(onClick = { /* No-op, should use ViewModel */ }) {
                    Icon(
                        imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                AndroidView(
                    factory = { imageView },
                    modifier = Modifier.size(50.dp)
                )

                Text(
                    text = "${weather.temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Text(
                text = weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                WeatherDataRow("Feels like", "${weather.feelsLike.toInt()}°C")
                WeatherDataRow(
                    "Min/Max",
                    "${weather.minTemp.toInt()}°C / ${weather.maxTemp.toInt()}°C"
                )
                WeatherDataRow("Humidity", "${weather.humidity}%")
                WeatherDataRow("Pressure", "${weather.pressure} hPa")
                WeatherDataRow("Wind", "${weather.windSpeed} m/s")
                WeatherDataRow("Sunrise", WeatherIconMapper.formatTimestamp(weather.sunriseTime))
                WeatherDataRow("Sunset", WeatherIconMapper.formatTimestamp(weather.sunsetTime))
            }
        }
    }

    LaunchedEffect(weather.icon) {
        val iconUrl = "https://openweathermap.org/img/wn/${weather.icon}@2x.png"
        ImageLoader.loadInto(iconUrl, imageView, scope)
    }
}

@Composable
private fun WeatherDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
