package edu.skku.map.carchecklab

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class NhtsaClient {
    private val client: OkHttpClient = OkHttpClient()
    private val gson: Gson = Gson()

    fun getRecalls(
        year: Int,
        make: String,
        model: String
    ): RecallResponse {
        val url: String =
            "https://api.nhtsa.gov/recalls/recallsByVehicle" +
                    "?make=" + encode(make) +
                    "&model=" + encode(model) +
                    "&modelYear=" + year.toString()

        val body: String = getBody(url)

        return gson.fromJson(body, RecallResponse::class.java) ?: RecallResponse()
    }

    fun getComplaints(
        year: Int,
        make: String,
        model: String
    ): ComplaintResponse {
        val url: String =
            "https://api.nhtsa.gov/complaints/complaintsByVehicle" +
                    "?make=" + encode(make) +
                    "&model=" + encode(model) +
                    "&modelYear=" + year.toString()

        val body: String = getBody(url)

        return gson.fromJson(body, ComplaintResponse::class.java) ?: ComplaintResponse()
    }

    fun getSafetyRating(
        year: Int,
        make: String,
        model: String
    ): String {
        try {
            val vehicleUrl: String =
                "https://api.nhtsa.gov/SafetyRatings/modelyear/" +
                        year.toString() +
                        "/make/" +
                        encode(make) +
                        "/model/" +
                        encode(model) +
                        "?format=json"

            val vehicleBody: String = getBody(vehicleUrl)
            val vehicleResponse: SafetyVehicleResponse =
                gson.fromJson(vehicleBody, SafetyVehicleResponse::class.java)
                    ?: SafetyVehicleResponse()

            val vehicleList: ArrayList<SafetyVehicle> =
                vehicleResponse.results ?: ArrayList()

            if (vehicleList.size == 0) {
                return "Not Rated"
            }

            val vehicleId: String = vehicleList[0].vehicleId ?: ""

            if (vehicleId.isEmpty()) {
                return "Not Rated"
            }

            val ratingUrl: String =
                "https://api.nhtsa.gov/SafetyRatings/VehicleId/" +
                        vehicleId +
                        "?format=json"

            val ratingBody: String = getBody(ratingUrl)
            val ratingResponse: SafetyRatingResponse =
                gson.fromJson(ratingBody, SafetyRatingResponse::class.java)
                    ?: SafetyRatingResponse()

            val ratingList: ArrayList<SafetyRatingItem> =
                ratingResponse.results ?: ArrayList()

            if (ratingList.size == 0) {
                return "Not Rated"
            }

            val overallRating: String = ratingList[0].overallRating ?: ""

            if (overallRating.isEmpty() || overallRating == "Not Rated") {
                return "Not Rated"
            }

            return overallRating

        } catch (e: Exception) {
            return "Not Rated"
        }
    }

    fun decodeVin(vin: String): VinDecodeResponse {
        val url: String =
            "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValuesExtended/" +
                    encode(vin) +
                    "?format=json"

        val body: String = getBody(url)

        return gson.fromJson(body, VinDecodeResponse::class.java) ?: VinDecodeResponse()
    }

    private fun getBody(url: String): String {
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "CarCheckLab Android Project")
            .build()

        client.newCall(request).execute().use { response ->
            return response.body?.string() ?: "{}"
        }
    }

    private fun encode(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }
}