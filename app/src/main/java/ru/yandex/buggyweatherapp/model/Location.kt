package ru.yandex.buggyweatherapp.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
) {

    override fun toString(): String {
        return buildString {
            append("Latitude: $latitude, ")
            append("Longitude: $longitude")
            name?.let {
                append(", Name: $it")
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (other !is Location) return false
        return latitude == other.latitude && longitude == other.longitude
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}