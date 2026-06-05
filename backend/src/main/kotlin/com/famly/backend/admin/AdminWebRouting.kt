package com.famly.backend.admin

import com.famly.backend.FamlyConfig
import com.famly.backend.models.AdminGrantSubscriptionRequest
import com.famly.backend.models.AdminLoginRequest
import com.famly.backend.models.AdminSyncLogSummaryDto
import com.famly.backend.models.AuthResponse
import com.famly.backend.services.AdminService
import com.famly.backend.services.AuthService
import com.famly.backend.services.HouseholdService
import com.famly.backend.services.SubscriptionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureAdminWebRouting(
    authService: AuthService,
    adminService: AdminService,
    householdService: HouseholdService,
    subscriptionService: SubscriptionService,
) {
    get("/static/admin.css") {
        val bytes = javaClass.classLoader.getResourceAsStream("admin/admin.css")?.readBytes()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respondBytes(bytes, ContentType.Text.CSS)
    }

    get("/static/logo.png") {
        val bytes = javaClass.classLoader.getResourceAsStream("admin/logo.png")?.readBytes()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respondBytes(bytes, ContentType.Image.PNG)
    }

    get("") {
        if (AdminAuth.readAdminToken(call) != null) {
            call.respondRedirect("/admin/dashboard")
        } else {
            call.respondText(AdminTemplates.loginPage(), ContentType.Text.Html)
        }
    }

    post("/login") {
        if (call.request.contentType().match(ContentType.Application.Json)) {
            val body = call.receive<AdminLoginRequest>()
            val (userId, token) = authService.adminLogin(body.email, body.password)
            adminService.audit(userId, "login_api")
            call.respond(AuthResponse(token, userId))
            return@post
        }
        val params = call.receiveParameters()
        val email = params["email"] ?: ""
        val password = params["password"] ?: ""
        try {
            val (userId, token) = authService.adminLogin(email, password)
            adminService.audit(userId, "login")
            AdminAuth.setAdminCookie(call, token)
            call.respondRedirect("/admin/dashboard")
        } catch (e: Exception) {
            val msg = when (e) {
                is IllegalStateException -> "Слишком много попыток. Подождите 15 минут."
                else -> "Неверный email или пароль"
            }
            call.respondText(
                AdminTemplates.loginPage(msg),
                ContentType.Text.Html,
                HttpStatusCode.Unauthorized,
            )
        }
    }

    post("/logout") {
        val token = AdminAuth.readAdminToken(call)
        if (token != null) {
            runCatching {
                val userId = authService.verifier.verify(token).getClaim("userId").asString()
                if (AdminAuth.validateCsrf(userId, call.receiveParameters()["csrf"])) {
                    adminService.audit(userId, "logout")
                }
            }
        }
        AdminAuth.clearAdminCookie(call)
        call.respondRedirect("/admin")
    }

    get("/users") {
        val isApi = adminApiRequest(call)
        val adminId = AdminAuth.resolveAdminUserId(call, authService)
            ?: return@get if (isApi) call.respond(HttpStatusCode.Unauthorized) else call.respondRedirect("/admin")
        if (isApi) {
            adminService.audit(adminId, "list_users_api")
            call.respond(adminService.listUsers())
        } else {
            adminService.audit(adminId, "list_users")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val q = call.request.queryParameters["q"]
            val pageSize = 25
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.usersPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    users = adminService.listUsers(page, pageSize, q),
                    page = page,
                    total = adminService.usersCount(q),
                    pageSize = pageSize,
                    query = q,
                ),
                ContentType.Text.Html,
            )
        }
    }

    get("/households") {
        val isApi = adminApiRequest(call)
        val adminId = AdminAuth.resolveAdminUserId(call, authService)
            ?: return@get if (isApi) call.respond(HttpStatusCode.Unauthorized) else call.respondRedirect("/admin")
        if (isApi) {
            adminService.audit(adminId, "list_households_api")
            call.respond(adminService.listHouseholds())
        } else {
            adminService.audit(adminId, "list_households")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = 25
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.householdsPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    households = adminService.listHouseholdsPage(page, pageSize),
                    page = page,
                    total = adminService.householdsCount(),
                    pageSize = pageSize,
                ),
                ContentType.Text.Html,
            )
        }
    }

    post("/households/{id}/regenerate-invite") {
        val isApi = adminApiRequest(call)
        val adminId = AdminAuth.resolveAdminUserId(call, authService)
            ?: return@post if (isApi) call.respond(HttpStatusCode.Unauthorized) else call.respondRedirect("/admin")
        if (!isApi && !AdminAuth.validateCsrf(adminId, call.receiveParameters()["csrf"])) {
            return@post call.respond(HttpStatusCode.Forbidden, "CSRF")
        }
        val id = call.parameters["id"]!!
        adminService.getHousehold(id) ?: return@post call.respond(HttpStatusCode.NotFound)
        val code = householdService.regenerateInviteCode(id)
        adminService.audit(
            adminId,
            if (isApi) "regenerate_invite_api" else "regenerate_invite",
            "household",
            id,
        )
        if (isApi) {
            call.respond(mapOf("inviteCode" to code))
        } else {
            call.respondRedirect(
                "/admin/households/$id?flash=${java.net.URLEncoder.encode("Invite-код обновлён", Charsets.UTF_8)}",
            )
        }
    }

    authenticate("admin-cookie") {
        get("/dashboard") {
            val adminId = call.requireAdminId()
            adminService.audit(adminId, "view_dashboard")
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.dashboard(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    stats = adminService.statsCached(),
                    chart = adminService.registrationsLast7Days(),
                ),
                ContentType.Text.Html,
            )
        }

        get("/households/{id}") {
            val adminId = call.requireAdminId()
            val id = call.parameters["id"]!!
            adminService.audit(adminId, "view_household", "household", id)
            val household = adminService.getHousehold(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Not found")
            val csrf = AdminAuth.issueCsrf(adminId)
            val flash = call.request.queryParameters["flash"]
            call.respondText(
                AdminTemplates.householdDetailPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    household = household,
                    transactions = adminService.transactionSummaries(id),
                    flash = flash,
                ),
                ContentType.Text.Html,
            )
        }

        get("/sync") {
            val adminId = call.requireAdminId()
            adminService.audit(adminId, "view_sync_log")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val type = call.request.queryParameters["type"]?.takeIf { it.isNotBlank() }
            val pageSize = 25
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.syncPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    entries = adminService.syncLogSummary(page, pageSize, type),
                    page = page,
                    total = adminService.syncLogCount(type),
                    pageSize = pageSize,
                    entityType = type,
                ),
                ContentType.Text.Html,
            )
        }

        get("/sync/{id}") {
            val adminId = call.requireAdminId()
            val id = call.parameters["id"]!!
            adminService.audit(adminId, "view_sync_entry", "sync", id)
            val entry = adminService.syncLogEntry(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val meta = AdminSyncLogSummaryDto(
                entry.id, entry.householdId, entry.entityType, entry.entityId, entry.updatedAt, entry.deleted,
            )
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.syncDetailPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    id = id,
                    payload = entry.payload,
                    meta = meta,
                ),
                ContentType.Text.Html,
            )
        }

        get("/audit") {
            val adminId = call.requireAdminId()
            adminService.audit(adminId, "view_audit")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = 25
            val csrf = AdminAuth.issueCsrf(adminId)
            call.respondText(
                AdminTemplates.auditPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    entries = adminService.listAudit(page, pageSize),
                    page = page,
                    total = adminService.auditCount(),
                    pageSize = pageSize,
                ),
                ContentType.Text.Html,
            )
        }

        get("/health") {
            val adminId = call.requireAdminId()
            adminService.audit(adminId, "view_health")
            val csrf = AdminAuth.issueCsrf(adminId)
            val uptime = System.currentTimeMillis() - AdminRuntime.startedAtMs
            call.respondText(
                AdminTemplates.healthPage(
                    adminEmail = authService.adminEmail(adminId) ?: adminId,
                    csrfToken = csrf,
                    serviceVersion = AdminRuntime.BACKEND_VERSION,
                    uptimeMs = uptime,
                    stats = adminService.statsCached(),
                ),
                ContentType.Text.Html,
            )
        }
    }

    authenticate("admin-jwt") {
        get("/stats") {
            call.respond(adminService.statsCached())
        }

        get("/sync-log") {
            val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            val entityType = call.request.queryParameters["type"]
            adminService.audit(adminId, "view_sync_log_api", details = entityType)
            call.respond(adminService.syncLog(entityType = entityType))
        }

        get("/sync-log/{id}") {
            val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            val id = call.parameters["id"]!!
            adminService.audit(adminId, "view_sync_entry_api", "sync", id)
            val entry = adminService.syncLogEntry(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(entry)
        }

        get("/transactions") {
            val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            val householdId = call.request.queryParameters["householdId"]
            adminService.audit(adminId, "view_transactions_api", "household", householdId)
            call.respond(adminService.transactions(householdId))
        }

        post("/subscriptions/grant") {
            if (!FamlyConfig.monetizationEnabled) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Monetization disabled"))
                return@post
            }
            val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            val body = call.receive<AdminGrantSubscriptionRequest>()
            subscriptionService.activate(body.userId, "admin", body.expiresAt)
            adminService.audit(adminId, "grant_subscription", "user", body.userId)
            call.respond(mapOf("status" to "granted"))
        }
    }
}

private fun adminApiRequest(call: ApplicationCall): Boolean {
    if (AdminAuth.wantsJsonApi(call)) return true
    if (call.request.headers[HttpHeaders.Authorization]?.startsWith("Bearer ", ignoreCase = true) == true) return true
    return call.request.contentType().match(ContentType.Application.Json)
}

private fun ApplicationCall.requireAdminId(): String =
    principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
