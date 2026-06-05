package com.famly.backend.plugins

import com.famly.backend.FamlyConfig
import com.famly.backend.models.AdminGrantSubscriptionRequest
import com.famly.backend.models.AdminLoginRequest
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

fun Route.configureAdminRouting(
    authService: AuthService,
    adminService: AdminService,
    subscriptionService: SubscriptionService,
    householdService: HouseholdService,
) {
    route("/admin") {
        post("/login") {
            val body = call.receive<AdminLoginRequest>()
            val (userId, token) = authService.adminLogin(body.email, body.password)
            adminService.audit(userId, "login")
            call.respond(AuthResponse(token, userId))
        }

        authenticate("admin-jwt") {
            get("/dashboard") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                adminService.audit(adminId, "view_dashboard")
                call.respondText(adminDashboardHtml(adminService.stats(), FamlyConfig.monetizationEnabled), ContentType.Text.Html)
            }

            get("/stats") {
                call.respond(adminService.stats())
            }

            get("/users") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                adminService.audit(adminId, "list_users")
                call.respond(adminService.listUsers())
            }

            get("/households") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                adminService.audit(adminId, "list_households")
                call.respond(adminService.listHouseholds())
            }

            get("/sync-log") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val entityType = call.request.queryParameters["type"]
                adminService.audit(adminId, "view_sync_log", details = entityType)
                call.respond(adminService.syncLog(entityType = entityType))
            }

            get("/transactions") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val householdId = call.request.queryParameters["householdId"]
                adminService.audit(adminId, "view_transactions", "household", householdId)
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

            post("/households/{id}/regenerate-invite") {
                val adminId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val id = call.parameters["id"]!!
                adminService.listHouseholds().find { it.id == id }
                    ?: throw IllegalArgumentException("Household not found")
                val code = householdService.regenerateInviteCode(id)
                adminService.audit(adminId, "regenerate_invite", "household", id)
                call.respond(mapOf("inviteCode" to code))
            }
        }
    }
}

private fun adminDashboardHtml(stats: com.famly.backend.models.AdminStatsResponse, monetizationEnabled: Boolean): String = """
    <!DOCTYPE html>
    <html lang="ru">
    <head>
      <meta charset="utf-8"/>
      <meta name="viewport" content="width=device-width, initial-scale=1"/>
      <title>Famly Admin</title>
      <style>
        body { font-family: system-ui, sans-serif; margin: 0; background: #f5f5f5; }
        header { background: #1B4332; color: white; padding: 16px 24px; }
        main { max-width: 960px; margin: 24px auto; padding: 0 16px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; }
        .card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
        .card h2 { margin: 0; font-size: 28px; color: #1B4332; }
        .card p { margin: 8px 0 0; color: #666; font-size: 14px; }
        nav { margin-top: 24px; }
        nav a { display: inline-block; margin-right: 12px; color: #1B4332; }
        .note { margin-top: 16px; padding: 12px 16px; background: #e8f5e9; border-radius: 8px; color: #1B4332; }
      </style>
    </head>
    <body>
      <header><h1>Famly Admin</h1></header>
      <main>
        ${if (!monetizationEnabled) """<div class="note">Монетизация отключена — весь функционал бесплатный для пользователей.</div>""" else ""}
        <div class="grid">
          <div class="card"><h2>${stats.usersCount}</h2><p>Пользователей</p></div>
          <div class="card"><h2>${stats.householdsCount}</h2><p>Семей</p></div>
          <div class="card"><h2>${stats.syncEvents24h}</h2><p>Sync за 24ч</p></div>
          ${if (monetizationEnabled) """<div class="card"><h2>${stats.activeSubscriptions}</h2><p>Активных подписок</p></div>""" else ""}
        </div>
        <nav>
          <p>JSON API (Bearer admin token):</p>
          <a href="/admin/stats">/admin/stats</a>
          <a href="/admin/users">/admin/users</a>
          <a href="/admin/households">/admin/households</a>
          <a href="/admin/sync-log">/admin/sync-log</a>
          <a href="/admin/transactions">/admin/transactions</a>
        </nav>
      </main>
    </body>
    </html>
""".trimIndent()
