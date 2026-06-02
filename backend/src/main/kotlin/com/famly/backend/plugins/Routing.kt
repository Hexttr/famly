package com.famly.backend.plugins

import com.famly.backend.models.*
import com.famly.backend.services.AuthService
import com.famly.backend.services.HouseholdService
import com.famly.backend.services.SubscriptionService
import com.famly.backend.services.SyncService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureRouting() {
    install(ContentNegotiation) { json() }

    val authService = AuthService()
    val householdService = HouseholdService()
    val subscriptionService = SubscriptionService()
    val syncService = SyncService()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "famly-backend"))
        }

        route("/auth") {
            post("/register") {
                val body = call.receive<RegisterRequest>()
                val (userId, token) = authService.register(body.email, body.password, body.displayName)
                call.respond(AuthResponse(token, userId))
            }
            post("/login") {
                val body = call.receive<LoginRequest>()
                val (userId, token) = authService.login(body.email, body.password)
                call.respond(AuthResponse(token, userId))
            }
        }

        authenticate("auth-jwt") {
            route("/households") {
                get("/mine") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId)
                    if (h == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "No household"))
                    } else {
                        call.respond(
                            HouseholdResponse(
                                h.id, h.name, h.ownerId,
                                householdService.members(h.id).map {
                                    HouseholdMemberDto(it.id, it.userId, it.displayName, it.role, it.visibility)
                                },
                            ),
                        )
                    }
                }
                post {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<CreateHouseholdRequest>()
                    val h = householdService.create(body.name, userId, "User")
                    call.respond(HouseholdResponse(h.id, h.name, h.ownerId, householdService.members(h.id).map {
                        HouseholdMemberDto(it.id, it.userId, it.displayName, it.role, it.visibility)
                    }))
                }
                post("/join") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<JoinHouseholdRequest>()
                    val h = householdService.join(body.inviteCode, userId, "User")
                    call.respond(HouseholdResponse(h.id, h.name, h.ownerId, householdService.members(h.id).map {
                        HouseholdMemberDto(it.id, it.userId, it.displayName, it.role, it.visibility)
                    }))
                }
                post("/{id}/invite") {
                    val id = call.parameters["id"]!!
                    val h = householdService.getForUser(call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString())
                    if (h?.id != id) {
                        call.respond(HttpStatusCode.Forbidden)
                    } else {
                        call.respond(InviteResponse(h.inviteCode, "https://famly.app/join/${h.inviteCode}"))
                    }
                }
            }

            route("/sync") {
                post("/push") {
                    val body = call.receive<SyncPushRequest>()
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId) ?: return@post call.respond(HttpStatusCode.Forbidden)
                    syncService.push(h.id, body.entities)
                    call.respond(HttpStatusCode.OK)
                }
                get("/pull") {
                    val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId) ?: return@get call.respond(HttpStatusCode.Forbidden)
                    val (entities, token) = syncService.pull(h.id, since)
                    call.respond(SyncPullResponse(entities, token))
                }
            }

            get("/subscription/status") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val sub = subscriptionService.status(userId)
                call.respond(SubscriptionStatusResponse(sub.isActive, sub.expiresAt, sub.source.ifBlank { null }))
            }
        }

        post("/webhooks/rustore") {
            val body = call.receive<RuStoreWebhookPayload>()
            when (body.event) {
                "SUBSCRIPTION_RENEWED", "SUBSCRIPTION_PURCHASED" ->
                    subscriptionService.activate(body.userId, "rustore", body.expiresAt)
                "SUBSCRIPTION_CANCELLED" -> subscriptionService.activate(body.userId, "rustore", null)
            }
            call.respond(HttpStatusCode.OK)
        }

        post("/webhooks/yookassa") {
            val body = call.receive<YooKassaWebhookPayload>()
            if (body.event == "payment.succeeded" && body.userId != null) {
                val expires = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
                subscriptionService.activate(body.userId, "yookassa", expires)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}
