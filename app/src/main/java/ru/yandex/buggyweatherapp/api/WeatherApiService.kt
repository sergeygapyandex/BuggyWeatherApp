package ru.yandex.buggyweatherapp.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>

    @GET("weather")
    fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>

    @GET("forecast")
    fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>
}
