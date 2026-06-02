package com.famly.backend.plugins

import com.famly.backend.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

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
    }
}
