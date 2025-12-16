package ru.yandex.buggyweatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.ui.components.DetailedWeatherCard
import ru.yandex.buggyweatherapp.ui.components.LocationSearch
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    DisposableEffect(Unit) {

        viewModel.initialize(context)

        onDispose {

        }
    }

    val weatherData by viewModel.weatherData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val cityName by viewModel.cityName.observeAsState("")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationSearch(
            onCitySearch = { city ->
                viewModel.searchWeatherByCity(city)
            },
            onLocationRequest = {
                viewModel.fetchCurrentLocationWeather()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))


        if (isLoading && weatherData == null) {
            Text("Loading weather data...")
        }


        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        weatherData?.let { weather ->
            DetailedWeatherCard(weather = weather, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            WeatherCard(
                weather = weather,
                cityName = cityName,
                viewModel = viewModel,
                onFavoriteClick = { viewModel.toggleFavorite() },
                onRefreshClick = { viewModel.fetchCurrentLocationWeather() }
            )
        }
    }
}

@Composable
fun WeatherCard(
    weather: WeatherData,
    cityName: String,
    viewModel: WeatherViewModel,
    onFavoriteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val backgroundColor = WeatherIconMapper.getBackgroundColor(weather.weatherId, weather.temperature)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
                    text = cityName.ifEmpty { weather.cityName },
                    style = MaterialTheme.typography.headlineMedium
                )

                Row {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }

                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = "Temperature: ${viewModel.formatTemperature(weather.temperature)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Feels like: ${viewModel.formatTemperature(weather.feelsLike)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = WeatherIconMapper.getWeatherDescription(
                    weather.description,
                    weather.temperature
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Humidity: ${weather.humidity}%",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Wind: ${weather.windSpeed} m/s",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sunrise: ${WeatherIconMapper.formatTimestamp(weather.sunriseTime)}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Sunset: ${WeatherIconMapper.formatTimestamp(weather.sunsetTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRefreshClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Refresh Weather")
            }
        }
    }
}
