package ru.yandex.buggyweatherapp.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository

class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()
    private var locationRepository: LocationRepository? = null

    val weatherData = MutableLiveData<WeatherData?>()
    val currentLocation = MutableLiveData<Location?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val cityName = MutableLiveData<String>()

    private var refreshJob: Job? = null

    fun initialize(context: Context) {
        locationRepository = LocationRepository(context.applicationContext)
        locationRepository?.startLocationTracking()
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun fetchCurrentLocationWeather() {
        isLoading.value = true
        error.value = null

        locationRepository?.getCurrentLocation { location ->
            if (location != null) {
                currentLocation.value = location

                viewModelScope.launch {
                    val cityNameFromLocation = locationRepository?.getCityNameFromLocation(location)
                    cityName.value = cityNameFromLocation ?: ""
                }

                getWeatherForLocation(location)
            } else {
                isLoading.value = false
                error.value = "Unable to get current location"
                currentLocation.value = null
            }
        }
    }

    fun getWeatherForLocation(location: Location) {
        isLoading.value = true
        error.value = null

        weatherRepository.getWeatherData(location) { data, exception ->
            isLoading.value = false

            if (data != null) {
                weatherData.value = data
            } else {
                error.value = exception?.message ?: "Unknown error"
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            error.value = "City name cannot be empty"
            return
        }

        isLoading.value = true
        error.value = null

        weatherRepository.getWeatherByCity(city) { data, exception ->
            isLoading.value = false

            if (data != null) {
                weatherData.value = data
                cityName.value = data.cityName
                currentLocation.value = Location(0.0, 0.0, data.cityName)
            } else {
                error.value = exception?.message ?: "Unknown error"
            }
        }
    }

    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60000)
                currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }
    }

    fun toggleFavorite() {
        weatherData.value?.let { currentData ->
            val updatedData = currentData.copy(isFavorite = !currentData.isFavorite)
            weatherData.value = updatedData
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        locationRepository = null
    }
}
