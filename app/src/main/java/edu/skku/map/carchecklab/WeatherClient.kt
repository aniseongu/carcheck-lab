package edu.skku.map.carchecklab

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

data class WeatherInfo(
    var city: String,
    var main: String,
    var description: String,
    var tempC: Double,
    var success: Boolean,
    var humidity: Int = 0,
    var windSpeed: Double = 0.0,
    var visibility: Int = 0
)

class WeatherClient {
    companion object {
        const val OPENWEATHER_API_KEY: String = "YOUR_OPENWEATHER_API_KEY"
    }

    private val client: OkHttpClient = OkHttpClient()

    fun getCurrentWeather(city: String): WeatherInfo {
        if (
            OPENWEATHER_API_KEY.isBlank() ||
            OPENWEATHER_API_KEY == "YOUR_OPENWEATHER_API_KEY"
        ) {
            return WeatherInfo(
                city,
                "Unknown",
                "API key not configured",
                0.0,
                false
            )
        }

        try {
            val normalizedCity = normalizeCity(city)

            val url: String =
                "https://api.openweathermap.org/data/2.5/weather" +
                        "?q=" + encode(normalizedCity) +
                        "&appid=" + OPENWEATHER_API_KEY +
                        "&units=metric"

            val request: Request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "CarCheckLab Android Project")
                .build()

            client.newCall(request).execute().use { response ->
                val body: String = response.body?.string() ?: "{}"

                if (!response.isSuccessful) {
                    return WeatherInfo(
                        normalizedCity,
                        "Unknown",
                        "Weather request failed: " + response.code.toString(),
                        0.0,
                        false
                    )
                }

                val jsonObject = JSONObject(body)

                val weatherArray = jsonObject.optJSONArray("weather")
                val weatherObject =
                    if (weatherArray != null && weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0)
                    } else {
                        JSONObject()
                    }

                val mainObject: JSONObject = jsonObject.optJSONObject("main") ?: JSONObject()
                val windObject: JSONObject = jsonObject.optJSONObject("wind") ?: JSONObject()

                val main: String = weatherObject.optString("main", "Unknown")
                val description: String = weatherObject.optString("description", "Unknown")
                val tempC: Double = mainObject.optDouble("temp", 0.0)
                val humidity: Int = mainObject.optInt("humidity", 0)
                val windSpeed: Double = windObject.optDouble("speed", 0.0)
                val visibility: Int = jsonObject.optInt("visibility", 0)
                val cityName: String = jsonObject.optString("name", normalizedCity)

                return WeatherInfo(
                    cityName,
                    main,
                    description,
                    tempC,
                    true,
                    humidity,
                    windSpeed,
                    visibility
                )
            }

        } catch (e: Exception) {
            return WeatherInfo(
                city,
                "Unknown",
                "Weather request failed",
                0.0,
                false
            )
        }
    }

    private fun normalizeCity(city: String): String {
        val clean = city.trim()

        if (clean.contains("수원")) return "Suwon,KR"
        if (clean.contains("서울")) return "Seoul,KR"
        if (clean.contains("인천")) return "Incheon,KR"
        if (clean.contains("부산")) return "Busan,KR"
        if (clean.contains("대구")) return "Daegu,KR"
        if (clean.contains("대전")) return "Daejeon,KR"
        if (clean.contains("광주")) return "Gwangju,KR"
        if (clean.contains("울산")) return "Ulsan,KR"
        if (clean.contains("성남")) return "Seongnam,KR"
        if (clean.contains("용인")) return "Yongin,KR"
        if (clean.contains("서산")) return "Seosan,KR"

        if (!clean.contains(",") && clean.length <= 20) {
            return "$clean,KR"
        }

        return clean
    }

    private fun encode(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }
}