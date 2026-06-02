package com.famly.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal HTTP client for Premium sync and subscription status.
 * Base URL configurable via BuildConfig in production.
 */
class FamlyApiClient(private val baseUrl: String = "http://10.0.2.2:8080") {

    suspend fun getSubscriptionStatus(token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$baseUrl/subscription/status").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
            }
            val body = conn.inputStream.bufferedReader().readText()
            JSONObject(body).getBoolean("isPremium")
        } catch (_: Exception) {
            false
        }
    }

    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = URL("$baseUrl/health").openConnection() as HttpURLConnection
            conn.responseCode == 200
        } catch (_: Exception) {
            false
        }
    }
}
