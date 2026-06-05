package com.famly.backend.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object AdminAuth {
    const val COOKIE_NAME = "famly_admin"
    const val COOKIE_MAX_AGE = 8 * 60 * 60
    const val COOKIE_PATH = "/admin"

    private data class CsrfEntry(val token: String, val expiresAt: Long)
    private val csrfTokens = ConcurrentHashMap<String, CsrfEntry>()
    private val csrfTtlMs = 60 * 60 * 1000L

    fun issueCsrf(userId: String): String {
        val token = UUID.randomUUID().toString()
        csrfTokens[userId] = CsrfEntry(token, System.currentTimeMillis() + csrfTtlMs)
        return token
    }

    fun validateCsrf(userId: String, token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        val entry = csrfTokens[userId] ?: return false
        if (System.currentTimeMillis() > entry.expiresAt) {
            csrfTokens.remove(userId)
            return false
        }
        return entry.token == token
    }

    fun setAdminCookie(call: ApplicationCall, token: String) {
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = token,
                path = COOKIE_PATH,
                maxAge = COOKIE_MAX_AGE,
                httpOnly = true,
                secure = isSecureRequest(call),
                extensions = mapOf("SameSite" to "Strict"),
            ),
        )
    }

    fun clearAdminCookie(call: ApplicationCall) {
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = "",
                path = COOKIE_PATH,
                maxAge = 0,
                httpOnly = true,
                secure = isSecureRequest(call),
                extensions = mapOf("SameSite" to "Strict"),
            ),
        )
    }

    private fun isSecureRequest(call: ApplicationCall): Boolean {
        if (call.request.local.scheme == "https") return true
        return call.request.headers["X-Forwarded-Proto"]?.equals("https", ignoreCase = true) == true
    }

    fun readAdminToken(call: ApplicationCall): String? =
        call.request.cookies[COOKIE_NAME]?.takeIf { it.isNotBlank() }

    fun resolveAdminUserId(call: ApplicationCall, authService: com.famly.backend.services.AuthService): String? {
        readAdminToken(call)?.let { token ->
            runCatching {
                val jwt = authService.verifier.verify(token)
                if (jwt.getClaim("role").asString() == "admin") {
                    return jwt.getClaim("userId").asString()
                }
            }
        }
        val header = call.request.headers[HttpHeaders.Authorization] ?: return null
        if (!header.startsWith("Bearer ", ignoreCase = true)) return null
        val token = header.removePrefix("Bearer ").trim()
        return runCatching {
            val jwt = authService.verifier.verify(token)
            if (jwt.getClaim("role").asString() == "admin") {
                jwt.getClaim("userId").asString()
            } else {
                null
            }
        }.getOrNull()
    }

    fun wantsJsonApi(call: ApplicationCall): Boolean {
        val accept = call.request.headers[HttpHeaders.Accept] ?: return false
        return accept.contains("application/json") && !accept.contains("text/html")
    }
}
