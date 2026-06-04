package com.famly.backend

import com.famly.backend.plugins.configureAuth
import com.famly.backend.plugins.configureDatabase
import com.famly.backend.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthIntegrationTest {
    @Test
    fun registerLoginAndCreateHousehold() = testApplication {
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@famly.app","password":"secret1","displayName":"Tester"}""")
        }
        assertEquals(HttpStatusCode.OK, registerResponse.status)
        assertTrue(registerResponse.bodyAsText().contains("token"))

        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@famly.app","password":"secret1"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = Regex(""""token":"([^"]+)"""").find(loginResponse.bodyAsText())!!.groupValues[1]

        val householdResponse = client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"name":"Test Family"}""")
        }
        assertEquals(HttpStatusCode.OK, householdResponse.status)
        assertTrue(householdResponse.bodyAsText().contains("Test Family"))
    }

    @Test
    fun healthCheck() = testApplication {
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("ok"))
    }
}
