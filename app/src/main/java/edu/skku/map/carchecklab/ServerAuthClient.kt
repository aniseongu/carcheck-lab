package edu.skku.map.carchecklab

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ServerAuthResult(
    var success: Boolean,
    var message: String,
    var userId: String,
    var city: String,
    var priority: String,
    var hasProfile: Boolean
)

class ServerAuthClient {
    companion object {
        const val AUTH_API_URL: String =
            "https://YOUR_API_ID.execute-api.YOUR_REGION.amazonaws.com/auth"
    }

    private val client: OkHttpClient = OkHttpClient()

    fun register(userId: String, password: String): ServerAuthResult {
        val body = JSONObject()
        body.put("action", "register")
        body.put("userId", userId)
        body.put("password", password)
        return requestServer(body)
    }

    fun login(userId: String, password: String): ServerAuthResult {
        val body = JSONObject()
        body.put("action", "login")
        body.put("userId", userId)
        body.put("password", password)
        return requestServer(body)
    }

    fun updateProfile(
        userId: String,
        city: String,
        priority: String
    ): ServerAuthResult {
        val body = JSONObject()
        body.put("action", "updateProfile")
        body.put("userId", userId)
        body.put("city", city)
        body.put("priority", priority)
        return requestServer(body)
    }

    fun getProfile(userId: String): ServerAuthResult {
        val body = JSONObject()
        body.put("action", "getProfile")
        body.put("userId", userId)
        return requestServer(body)
    }

    private fun requestServer(body: JSONObject): ServerAuthResult {
        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = body.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(AUTH_API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "CarCheckLab Android Project")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody: String = response.body?.string() ?: ""

                if (responseBody.isEmpty()) {
                    return ServerAuthResult(
                        false,
                        "Empty server response.",
                        "",
                        "",
                        "",
                        false
                    )
                }

                val jsonObject = JSONObject(responseBody)

                return ServerAuthResult(
                    jsonObject.optBoolean("success", false),
                    jsonObject.optString("message", "No message"),
                    jsonObject.optString("userId", ""),
                    jsonObject.optString("city", ""),
                    jsonObject.optString("priority", ""),
                    jsonObject.optBoolean("hasProfile", false)
                )
            }

        } catch (e: Exception) {
            return ServerAuthResult(
                false,
                "Server request failed: " + e.message,
                "",
                "",
                "",
                false
            )
        }
    }
}