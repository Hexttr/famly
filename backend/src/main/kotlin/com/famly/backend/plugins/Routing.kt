package com.famly.backend.plugins

import com.famly.backend.FamlyConfig
import com.famly.backend.models.*
import com.famly.backend.services.AccessDeniedException
import com.famly.backend.services.AdminService
import com.famly.backend.services.AuthService
import com.famly.backend.services.HouseholdService
import com.famly.backend.services.SubscriptionService
import com.famly.backend.services.SyncService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }
    install(StatusPages) {
        exception<AccessDeniedException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to (cause.message ?: "Forbidden")))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Bad request")))
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, mapOf("error" to (cause.message ?: "Conflict")))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error")),
            )
        }
    }

    val authService = AuthService()
    val householdService = HouseholdService()
    val subscriptionService = SubscriptionService()
    val syncService = SyncService()
    val adminService = AdminService()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "famly-backend", "version" to "1.1.0"))
        }

        get("/join") {
            val code = call.request.queryParameters["code"]
            if (code.isNullOrBlank()) {
                call.respondText(joinLandingHtml(null), ContentType.Text.Html)
            } else {
                call.respondText(joinLandingHtml(code), ContentType.Text.Html)
            }
        }

        get("/legal/privacy") {
            call.respondText(loadLegalResource("privacy-policy.html"), ContentType.Text.Html)
        }
        get("/legal/terms") {
            call.respondText(loadLegalResource("terms-of-service.html"), ContentType.Text.Html)
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
            post("/logout") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }

        authenticate("auth-jwt") {
            route("/auth") {
                get("/profile") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val (name, email) = authService.profile(userId)
                    call.respond(ProfileResponse(name, email))
                }
                patch("/profile") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<UpdateProfileRequest>()
                    val name = authService.updateDisplayName(userId, body.displayName)
                    call.respond(ProfileResponse(name, authService.profile(userId).second))
                }
            }
            route("/households") {
                get("/mine") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId)
                    if (h == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "No household"))
                    } else {
                        call.respond(toHouseholdResponse(h, householdService))
                    }
                }
                post {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<CreateHouseholdRequest>()
                    val displayName = authService.displayName(userId)
                    val h = householdService.create(body.name, userId, displayName)
                    call.respond(toHouseholdResponse(h, householdService))
                }
                post("/join") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<JoinHouseholdRequest>()
                    val displayName = authService.displayName(userId)
                    val h = householdService.join(body.inviteCode, userId, displayName)
                    call.respond(toHouseholdResponse(h, householdService))
                }
                post("/leave") {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    householdService.leave(userId)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "left"))
                }
                post("/{id}/invite") {
                    val id = call.parameters["id"]!!
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId)
                    if (h?.id != id) {
                        call.respond(HttpStatusCode.Forbidden)
                    } else {
                        val member = householdService.memberForUser(id, userId)
                        if (member == null) {
                            call.respond(HttpStatusCode.Forbidden)
                        } else {
                            val regenerate = call.request.queryParameters["regenerate"] == "true"
                            if (regenerate && member.role != "admin") {
                                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin role required"))
                            } else {
                                val code = if (regenerate) {
                                    householdService.regenerateInvite(id, userId)
                                } else {
                                    h.inviteCode
                                }
                                call.respond(
                                    InviteResponse(
                                        code,
                                        "${FamlyConfig.publicBaseUrl}/join?code=$code",
                                    ),
                                )
                            }
                        }
                    }
                }
                get("/{id}/members") {
                    val id = call.parameters["id"]!!
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId)
                    if (h?.id != id) {
                        call.respond(HttpStatusCode.Forbidden)
                    } else {
                        call.respond(
                            householdService.members(id).map {
                                HouseholdMemberDto(it.id, it.userId, it.displayName, it.role, it.visibility, it.avatar)
                            },
                        )
                    }
                }
                patch("/{id}/members/{memberId}") {
                    val id = call.parameters["id"]!!
                    val memberId = call.parameters["memberId"]!!
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val body = call.receive<UpdateMemberRequest>()
                    val updated = householdService.updateMember(
                        id, memberId, userId, body.role, body.visibility, body.displayName, body.avatar,
                    )
                    call.respond(
                        HouseholdMemberDto(
                            updated.id,
                            updated.userId,
                            updated.displayName,
                            updated.role,
                            updated.visibility,
                            updated.avatar,
                        ),
                    )
                }
                delete("/{id}/members/{memberId}") {
                    val id = call.parameters["id"]!!
                    val memberId = call.parameters["memberId"]!!
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    householdService.removeMember(id, memberId, userId)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "removed"))
                }
            }

            route("/sync") {
                post("/push") {
                    val body = call.receive<SyncPushRequest>()
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId) ?: return@post call.respond(HttpStatusCode.Forbidden)
                    val result = syncService.push(h.id, body.entities)
                    call.respond(SyncPushResponse(result.accepted, result.rejected))
                }
                get("/pull") {
                    val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val h = householdService.getForUser(userId) ?: return@get call.respond(HttpStatusCode.Forbidden)
                    val pull = syncService.pull(h.id, since)
                    val members = householdService.members(h.id).map {
                        HouseholdMemberDto(
                            it.id, it.userId, it.displayName, it.role, it.visibility, it.avatar,
                        )
                    }
                    call.respond(
                        SyncPullResponse(
                            entities = pull.entities,
                            syncToken = pull.syncToken,
                            household = HouseholdSnapshot(h.id, h.name, members),
                        ),
                    )
                }
            }

            get("/subscription/status") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                if (!FamlyConfig.monetizationEnabled) {
                    call.respond(SubscriptionStatusResponse(isPremium = true, expiresAt = null, source = null))
                } else {
                    val sub = subscriptionService.status(userId)
                    call.respond(SubscriptionStatusResponse(sub.isActive, sub.expiresAt, sub.source.ifBlank { null }))
                }
            }
        }

        post("/webhooks/rustore") {
            if (!FamlyConfig.monetizationEnabled) {
                call.respond(HttpStatusCode.OK)
                return@post
            }
            verifyWebhookSecret(call, "RUSTORE_WEBHOOK_SECRET", "X-RuStore-Signature")
            val body = call.receive<RuStoreWebhookPayload>()
            when (body.event) {
                "SUBSCRIPTION_RENEWED", "SUBSCRIPTION_PURCHASED" ->
                    subscriptionService.activate(body.userId, "rustore", body.expiresAt)
                "SUBSCRIPTION_CANCELLED" -> subscriptionService.activate(body.userId, "rustore", null)
            }
            call.respond(HttpStatusCode.OK)
        }

        post("/webhooks/yookassa") {
            if (!FamlyConfig.monetizationEnabled) {
                call.respond(HttpStatusCode.OK)
                return@post
            }
            verifyWebhookSecret(call, "YOOKASSA_WEBHOOK_SECRET", "X-YooKassa-Signature")
            val body = call.receive<YooKassaWebhookPayload>()
            if (body.event == "payment.succeeded" && body.userId != null) {
                val expires = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
                subscriptionService.activate(body.userId, "yookassa", expires)
            }
            call.respond(HttpStatusCode.OK)
        }

        configureAdminRouting(authService, adminService, subscriptionService, householdService)
    }
}

private fun toHouseholdResponse(
    h: HouseholdService.HouseholdRecord,
    householdService: HouseholdService,
) = HouseholdResponse(
    h.id, h.name, h.ownerId,
    householdService.members(h.id).map {
        HouseholdMemberDto(it.id, it.userId, it.displayName, it.role, it.visibility, it.avatar)
    },
)

private fun verifyWebhookSecret(call: io.ktor.server.application.ApplicationCall, envKey: String, headerName: String) {
    val secret = System.getenv(envKey) ?: return
    val provided = call.request.header(headerName)
    if (provided != secret) throw IllegalArgumentException("Invalid webhook signature")
}

private fun loadLegalResource(name: String): String {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("legal/$name")
        ?: return "<html><body><h1>Document not found</h1></body></html>"
    return stream.bufferedReader().use { it.readText() }
}

private fun joinLandingHtml(code: String?): String {
    val deepLink = code?.let { "famly://join?code=$it" } ?: "famly://join"
    val displayCode = code ?: ""
    return """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1"/>
          <title>Присоединиться к семье — Famly</title>
          <script>window.location.replace("$deepLink");</script>
          <style>
            body { font-family: system-ui, sans-serif; max-width: 480px; margin: 40px auto; padding: 0 16px; }
            .btn { display: block; background: #1B4332; color: white; text-align: center; padding: 14px; border-radius: 12px; text-decoration: none; font-weight: 600; margin-top: 16px; }
            code { background: #f0f0f0; padding: 4px 8px; border-radius: 6px; }
          </style>
        </head>
        <body>
          <h1>Мой (Наш) Бюджет</h1>
          ${if (code != null) "<p>Код приглашения: <code>$displayCode</code></p>" else "<p>Откройте ссылку из приглашения в приложении.</p>"}
          <a class="btn" href="$deepLink">Открыть в приложении</a>
          <p style="color:#666;font-size:14px;margin-top:24px;">Если приложение не установлено, скачайте «Мой (Наш) Бюджет» в RuStore.</p>
        </body>
        </html>
    """.trimIndent()
}
