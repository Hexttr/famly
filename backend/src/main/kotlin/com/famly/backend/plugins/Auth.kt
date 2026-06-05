package com.famly.backend.plugins

import com.famly.backend.admin.AdminAuth
import com.famly.backend.services.AuthService
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuth() {
    val authService = AuthService()
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(authService.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId.isNotBlank()) JWTPrincipal(credential.payload) else null
            }
        }
        jwt("admin-jwt") {
            verifier(authService.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val role = credential.payload.getClaim("role").asString()
                if (userId.isNotBlank() && role == "admin") JWTPrincipal(credential.payload) else null
            }
        }
        jwt("admin-cookie") {
            verifier(authService.verifier)
            authHeader { call ->
                AdminAuth.readAdminToken(call)?.let { HttpAuthHeader.Single("Bearer", it) }
            }
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val role = credential.payload.getClaim("role").asString()
                if (userId.isNotBlank() && role == "admin") JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respondRedirect("/admin")
            }
        }
    }
}
