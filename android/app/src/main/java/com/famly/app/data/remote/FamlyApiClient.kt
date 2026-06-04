package com.famly.app.data.remote

import com.famly.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AuthResult(val token: String, val userId: String)

data class HouseholdResult(
    val id: String,
    val name: String,
    val ownerId: String,
)

data class HouseholdMemberDto(
    val id: String,
    val userId: String,
    val displayName: String,
    val role: String,
    val visibility: String,
)

data class HouseholdFullResult(
    val id: String,
    val name: String,
    val ownerId: String,
    val members: List<HouseholdMemberDto>,
)

data class SyncEntityDto(
    val type: String,
    val id: String,
    val payload: String,
    val syncVersion: Int,
    val updatedAt: Long,
    val deleted: Boolean = false,
)

data class SyncPullResult(
    val entities: List<SyncEntityDto>,
    val syncToken: Long,
)

/**
 * HTTP client for auth, household management, and sync.
 * Base URL configurable; defaults to Android emulator host alias.
 */
class FamlyApiClient(private val baseUrl: String = BuildConfig.API_BASE_URL) {

    suspend fun register(email: String, password: String, displayName: String): AuthResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("displayName", displayName)
            }
            val response = postJson("/auth/register", body, authToken = null)
            AuthResult(response.getString("token"), response.getString("userId"))
        }

    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val response = postJson("/auth/login", body, authToken = null)
            AuthResult(response.getString("token"), response.getString("userId"))
        }

    suspend fun createHousehold(token: String, name: String): HouseholdResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply { put("name", name) }
            val response = postJson("/households", body, authToken = token)
            HouseholdResult(
                id = response.getString("id"),
                name = response.getString("name"),
                ownerId = response.getString("ownerId"),
            )
        }

    suspend fun joinHousehold(token: String, inviteCode: String): HouseholdResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply { put("inviteCode", inviteCode) }
            val response = postJson("/households/join", body, authToken = token)
            HouseholdResult(
                id = response.getString("id"),
                name = response.getString("name"),
                ownerId = response.getString("ownerId"),
            )
        }

    suspend fun generateInvite(token: String, householdId: String): String =
        withContext(Dispatchers.IO) {
            val response = postJson("/households/$householdId/invite", JSONObject(), authToken = token)
            response.getString("inviteCode")
        }

    suspend fun getHousehold(token: String): HouseholdFullResult? =
        withContext(Dispatchers.IO) {
            try {
                val response = getJson("/households/mine", authToken = token)
                val members = response.getJSONArray("members").let { arr ->
                    List(arr.length()) { i ->
                        val obj = arr.getJSONObject(i)
                        HouseholdMemberDto(
                            id = obj.getString("id"),
                            userId = obj.getString("userId"),
                            displayName = obj.getString("displayName"),
                            role = obj.getString("role"),
                            visibility = obj.getString("visibility"),
                        )
                    }
                }
                HouseholdFullResult(
                    id = response.getString("id"),
                    name = response.getString("name"),
                    ownerId = response.getString("ownerId"),
                    members = members,
                )
            } catch (_: Exception) {
                null
            }
        }

    suspend fun push(token: String, entities: List<SyncEntityDto>): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val arr = JSONArray()
                entities.forEach { e ->
                    arr.put(
                        JSONObject().apply {
                            put("type", e.type)
                            put("id", e.id)
                            put("payload", e.payload)
                            put("syncVersion", e.syncVersion)
                            put("updatedAt", e.updatedAt)
                            put("deleted", e.deleted)
                        },
                    )
                }
                postJson("/sync/push", JSONObject().put("entities", arr), authToken = token)
                true
            } catch (_: Exception) {
                false
            }
        }

    suspend fun pull(token: String, since: Long): SyncPullResult =
        withContext(Dispatchers.IO) {
            val response = getJson("/sync/pull?since=$since", authToken = token)
            val entities = response.getJSONArray("entities").let { arr ->
                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    SyncEntityDto(
                        type = obj.getString("type"),
                        id = obj.getString("id"),
                        payload = obj.getString("payload"),
                        syncVersion = obj.getInt("syncVersion"),
                        updatedAt = obj.getLong("updatedAt"),
                        deleted = obj.optBoolean("deleted", false),
                    )
                }
            }
            SyncPullResult(entities, response.getLong("syncToken"))
        }

    suspend fun getSubscriptionStatus(token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = getJson("/subscription/status", authToken = token)
            response.getBoolean("isPremium")
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

    private fun postJson(path: String, body: JSONObject, authToken: String?): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        return readResponse(conn)
    }

    private fun getJson(path: String, authToken: String?): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        return readResponse(conn)
    }

    private fun readResponse(conn: HttpURLConnection): JSONObject {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
        if (conn.responseCode !in 200..299) {
            val message = parseErrorMessage(text).ifBlank { text.ifBlank { "Неизвестная ошибка" } }
            error("HTTP ${conn.responseCode}: $message")
        }
        return JSONObject(text)
    }

    private fun parseErrorMessage(body: String): String {
        if (body.isBlank()) return ""
        return runCatching {
            val json = JSONObject(body)
            json.optString("error").ifBlank { json.optString("message") }
        }.getOrDefault(body)
    }
}
