package com.famly.app.data.remote

import com.famly.app.BuildConfig
import com.famly.app.domain.InviteLinks
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

data class ProfileResult(val displayName: String, val email: String)

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
    val avatar: String = "",
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

data class InviteResult(val inviteCode: String, val inviteUrl: String)

data class SubscriptionStatusResult(
    val isPremium: Boolean,
    val expiresAt: Long?,
)

data class SyncPullResult(
    val entities: List<SyncEntityDto>,
    val syncToken: Long,
    val household: HouseholdSnapshotDto? = null,
)

data class HouseholdSnapshotDto(
    val id: String,
    val name: String,
    val members: List<HouseholdMemberDto>,
)

data class SyncPushResult(
    val accepted: List<String>,
    val rejected: List<String>,
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

    suspend fun generateInvite(token: String, householdId: String): InviteResult =
        withContext(Dispatchers.IO) {
            val response = postJson("/households/$householdId/invite", JSONObject(), authToken = token)
            InviteResult(
                inviteCode = response.getString("inviteCode"),
                inviteUrl = response.optString("inviteUrl", InviteLinks.httpsJoinUrl(response.getString("inviteCode"))),
            )
        }

    suspend fun leaveHousehold(token: String) = withContext(Dispatchers.IO) {
        postJson("/households/leave", JSONObject(), authToken = token)
    }

    suspend fun updateMember(
        token: String,
        householdId: String,
        memberId: String,
        role: String? = null,
        visibility: String? = null,
        displayName: String? = null,
        avatar: String? = null,
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject()
        role?.let { body.put("role", it) }
        visibility?.let { body.put("visibility", it) }
        displayName?.let { body.put("displayName", it) }
        avatar?.let { body.put("avatar", it) }
        patchJson("/households/$householdId/members/$memberId", body, authToken = token)
    }

    suspend fun logout(token: String) = withContext(Dispatchers.IO) {
        postJson("/auth/logout", JSONObject(), authToken = token)
    }

    suspend fun updateProfile(token: String, displayName: String): ProfileResult =
        withContext(Dispatchers.IO) {
            val response = patchJson(
                "/auth/profile",
                JSONObject().apply { put("displayName", displayName) },
                authToken = token,
            )
            ProfileResult(
                displayName = response.getString("displayName"),
                email = response.getString("email"),
            )
        }

    suspend fun getProfile(token: String): ProfileResult? =
        withContext(Dispatchers.IO) {
            try {
                val response = getJson("/auth/profile", authToken = token)
                ProfileResult(
                    displayName = response.getString("displayName"),
                    email = response.getString("email"),
                )
            } catch (_: Exception) {
                null
            }
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
                            avatar = obj.optString("avatar", ""),
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

    suspend fun push(token: String, entities: List<SyncEntityDto>): SyncPushResult =
        withContext(Dispatchers.IO) {
            if (entities.isEmpty()) return@withContext SyncPushResult(emptyList(), emptyList())
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
            val response = postJson("/sync/push", JSONObject().put("entities", arr), authToken = token)
            SyncPushResult(
                accepted = response.optJSONArray("accepted").toStringList(),
                rejected = response.optJSONArray("rejected").toStringList(),
            )
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
            val household = if (response.has("household") && !response.isNull("household")) {
                val h = response.getJSONObject("household")
                val members = h.getJSONArray("members").let { arr ->
                    List(arr.length()) { i ->
                        val obj = arr.getJSONObject(i)
                        HouseholdMemberDto(
                            id = obj.getString("id"),
                            userId = obj.getString("userId"),
                            displayName = obj.getString("displayName"),
                            role = obj.getString("role"),
                            visibility = obj.getString("visibility"),
                            avatar = obj.optString("avatar", ""),
                        )
                    }
                }
                HouseholdSnapshotDto(
                    id = h.getString("id"),
                    name = h.getString("name"),
                    members = members,
                )
            } else {
                null
            }
            SyncPullResult(entities, response.getLong("syncToken"), household)
        }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return List(length()) { i -> getString(i) }
    }

    suspend fun getSubscriptionStatus(token: String): SubscriptionStatusResult = withContext(Dispatchers.IO) {
        try {
            val response = getJson("/subscription/status", authToken = token)
            SubscriptionStatusResult(
                isPremium = response.getBoolean("isPremium"),
                expiresAt = if (response.has("expiresAt") && !response.isNull("expiresAt")) {
                    response.getLong("expiresAt")
                } else {
                    null
                },
            )
        } catch (_: Exception) {
            SubscriptionStatusResult(isPremium = false, expiresAt = null)
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

    private fun patchJson(path: String, body: JSONObject, authToken: String?): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        return readResponse(conn, expectBody = true)
    }

    private fun postJson(path: String, body: JSONObject, authToken: String?): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        return readResponse(conn, expectBody = true)
    }

    private fun getJson(path: String, authToken: String?): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        return readResponse(conn, expectBody = true)
    }

    private fun readResponse(conn: HttpURLConnection, expectBody: Boolean = true): JSONObject {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
        if (conn.responseCode !in 200..299) {
            val message = parseErrorMessage(text).ifBlank { text.ifBlank { "Неизвестная ошибка" } }
            error("HTTP ${conn.responseCode}: $message")
        }
        if (!expectBody || text.isBlank()) return JSONObject()
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
