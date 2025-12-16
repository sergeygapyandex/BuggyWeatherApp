package ru.yandex.buggyweatherapp.repository

import android.util.Log
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.yandex.buggyweatherapp.api.RetrofitInstance
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData

private fun JsonObject.getSafeString(key: String): String? {
    val element = get(key)
    return if (element != null && !element.isJsonNull) element.asString else null
}

private fun JsonObject.getSafeDouble(key: String): Double? {
    val element = get(key)
    return if (element != null && !element.isJsonNull) element.asDouble else null
}

private fun JsonObject.getSafeInt(key: String): Int? {
    val element = get(key)
    return if (element != null && !element.isJsonNull) element.asInt else null
}

private fun JsonObject.getSafeLong(key: String): Long? {
    val element = get(key)
    return if (element != null && !element.isJsonNull) element.asLong else null
}

class WeatherRepository {
    private val weatherApi = RetrofitInstance.weatherApi

    private var cachedWeatherData: WeatherData? = null

    fun getWeatherData(location: Location, callback: (WeatherData?, Exception?) -> Unit) {
        val call = weatherApi.getCurrentWeather(location.latitude, location.longitude)

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val weatherData = parseWeatherData(response.body()!!, location)
                        cachedWeatherData = weatherData
                        callback(weatherData, null)
                    } catch (e: Exception) {
                        Log.e("WeatherRepository", "Error parsing weather data", e)
                        callback(null, e)
                    }
                } else {
                    callback(null, Exception("API Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("WeatherRepository", "Error fetching weather", t)
                callback(null, Exception(t))
            }
        })
    }

    fun getWeatherByCity(cityName: String, callback: (WeatherData?, Exception?) -> Unit) {
        weatherApi.getWeatherByCity(cityName).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val json = response.body()!!
                        val location = extractLocationFromResponse(json)
                        val weatherData = parseWeatherData(json, location)
                        callback(weatherData, null)
                    } catch (e: Exception) {

                        callback(null, e)
                    }
                } else {
                    callback(null, Exception("Error fetching weather data"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                callback(null, Exception(t))
            }
        })
    }

    private fun parseWeatherData(json: JsonObject, location: Location): WeatherData {
        val main = json.getAsJsonObject("main")
        val wind = json.getAsJsonObject("wind")
        val sys = json.getAsJsonObject("sys")
        val weather = json.getAsJsonArray("weather").get(0).asJsonObject
        val clouds = json.getAsJsonObject("clouds")

        val cityName = json.getSafeString("name") ?: location.name ?: "Unknown"

        return WeatherData(
            cityName = cityName,
            country = sys.getSafeString("country") ?: "",
            temperature = main.getSafeDouble("temp") ?: 0.0,
            feelsLike = main.getSafeDouble("feels_like") ?: 0.0,
            minTemp = main.getSafeDouble("temp_min") ?: 0.0,
            maxTemp = main.getSafeDouble("temp_max") ?: 0.0,
            humidity = main.getSafeInt("humidity") ?: 0,
            pressure = main.getSafeInt("pressure") ?: 0,
            windSpeed = wind.getSafeDouble("speed") ?: 0.0,
            windDirection = wind.getSafeInt("deg") ?: 0,
            description = weather.getSafeString("description") ?: "",
            icon = weather.getSafeString("icon") ?: "",
            weatherId = weather.getSafeInt("id") ?: 0,
            cloudiness = clouds.getSafeInt("all") ?: 0,
            sunriseTime = sys.getSafeLong("sunrise") ?: 0L,
            sunsetTime = sys.getSafeLong("sunset") ?: 0L,
            timezone = json.getSafeInt("timezone") ?: 0,
            timestamp = json.getSafeLong("dt") ?: 0L,
            rain = if (json.has("rain") && json.getAsJsonObject("rain").has("1h"))
                json.getAsJsonObject("rain").getSafeDouble("1h") else null,
            snow = if (json.has("snow") && json.getAsJsonObject("snow").has("1h"))
                json.getAsJsonObject("snow").getSafeDouble("1h") else null
        )
    }

    private fun extractLocationFromResponse(json: JsonObject): Location {
        val coord = json.getAsJsonObject("coord")
        val lat = coord.getSafeDouble("lat") ?: 0.0
        val lon = coord.getSafeDouble("lon") ?: 0.0
        val name = json.getSafeString("name") ?: "Unknown"

        return Location(lat, lon, name)
    }
}
