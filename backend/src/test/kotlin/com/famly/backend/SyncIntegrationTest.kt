package com.famly.backend

import com.famly.backend.plugins.configureAuth
import com.famly.backend.plugins.configureDatabase
import com.famly.backend.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.util.UUID

class SyncIntegrationTest {
    @Test
    fun pullReturnsEntityEvenWhenClientUpdatedAtIsBeforePullCursor() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_sync_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"alice@famly.app","password":"secret1","displayName":"User"}""")
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"alice@famly.app","password":"secret1"}""")
        }
        val token = Regex(""""token":"([^"]+)"""").find(loginResponse.bodyAsText())!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"name":"Ambaryan"}""")
        }

        val pull1Body = client.get("/sync/pull?since=0") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val cursorAfterEmptyPull = Regex(""""syncToken":(\d+)""").find(pull1Body)!!.groupValues[1].toLong()

        val staleClientTime = cursorAfterEmptyPull - 60_000
        val pushResponse = client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-stale-time",
                    "payload":"{\"id\":\"tx-stale-time\",\"amountKopecks\":2500,\"type\":\"expense\",\"categoryId\":\"c2\",\"accountId\":\"a_main\",\"dateEpochDay\":20609,\"note\":null,\"isRecurring\":false,\"isPrivate\":false,\"createdAt\":$staleClientTime,\"updatedAt\":$staleClientTime}",
                    "syncVersion":1,
                    "updatedAt":$staleClientTime,
                    "deleted":false
                  }]
                }""",
            )
        }
        assertEquals(HttpStatusCode.OK, pushResponse.status)
        assertTrue(pushResponse.bodyAsText().contains("accepted"))

        val pull2Body = client.get("/sync/pull?since=$cursorAfterEmptyPull") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        assertTrue(
            pull2Body.contains("tx-stale-time"),
            "Transaction must be returned even when client updatedAt is before pull cursor",
        )
    }
}
