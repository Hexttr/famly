package com.famly.backend

import com.famly.backend.db.Users
import com.famly.backend.plugins.configureAuth
import com.famly.backend.plugins.configureDatabase
import com.famly.backend.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AdminWebTest {
    private val adminEmail = "admin-web@test.local"
    private val adminPassword = "adminpass1"

    private fun ApplicationTestBuilder.setupWithAdmin() {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_admin_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            seedTestAdmin()
            configureAuth()
            configureRouting()
        }
    }

    private fun Application.seedTestAdmin() {
        transaction {
            val normalized = adminEmail.lowercase()
            if (Users.selectAll().where { Users.email eq normalized }.count() > 0) return@transaction
            Users.insert {
                it[Users.id] = UUID.randomUUID().toString()
                it[Users.email] = normalized
                it[Users.passwordHash] = BCrypt.hashpw(adminPassword, BCrypt.gensalt(12))
                it[Users.displayName] = "Test Admin"
                it[Users.isAdmin] = true
                it[Users.createdAt] = System.currentTimeMillis()
            }
        }
    }

    @Test
    fun adminLoginSetsCookieAndOpensDashboard() = testApplication {
        setupWithAdmin()
        val login = client.post("/admin/login") {
            setBody(
                Parameters.build {
                    append("email", adminEmail)
                    append("password", adminPassword)
                }.formUrlEncode(),
            )
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        assertEquals(HttpStatusCode.Found, login.status)
        assertTrue(login.headers[HttpHeaders.SetCookie]?.contains("famly_admin=") == true)

        val dashboard = client.get("/admin/dashboard")
        assertEquals(HttpStatusCode.OK, dashboard.status)
        assertTrue(dashboard.bodyAsText().contains("Famly Admin"))
    }

    @Test
    fun adminLoginFailReturnsUnauthorized() = testApplication {
        setupWithAdmin()
        val login = client.post("/admin/login") {
            setBody(
                Parameters.build {
                    append("email", adminEmail)
                    append("password", "wrong-password")
                }.formUrlEncode(),
            )
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, login.status)
        assertTrue(login.bodyAsText().contains("Неверный email или пароль"))
    }

    @Test
    fun dashboardWithoutCookieRedirectsToLogin() = testApplication {
        setupWithAdmin()
        val noRedirectClient = createClient { followRedirects = false }
        val response = noRedirectClient.get("/admin/dashboard")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/admin", response.headers[HttpHeaders.Location])
    }

    @Test
    fun regenerateInviteRejectsMissingCsrf() = testApplication {
        setupWithAdmin()
        val login = client.post("/admin/login") {
            setBody(
                Parameters.build {
                    append("email", adminEmail)
                    append("password", adminPassword)
                }.formUrlEncode(),
            )
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        assertEquals(HttpStatusCode.Found, login.status)
        val sessionCookie = login.headers[HttpHeaders.SetCookie]!!.substringBefore(";")
        val response = client.post("/admin/households/nonexistent/regenerate-invite") {
            header(HttpHeaders.Cookie, sessionCookie)
            setBody("csrf=bad-token")
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun adminLoginRateLimitAfterManyAttempts() = testApplication {
        setupWithAdmin()
        repeat(10) {
            client.post("/admin/login") {
                setBody(
                    Parameters.build {
                        append("email", adminEmail)
                        append("password", "wrong")
                    }.formUrlEncode(),
                )
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            }
        }
        val blocked = client.post("/admin/login") {
            setBody(
                Parameters.build {
                    append("email", adminEmail)
                    append("password", "wrong")
                }.formUrlEncode(),
            )
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, blocked.status)
        assertTrue(blocked.bodyAsText().contains("Слишком много попыток"))
    }
}
