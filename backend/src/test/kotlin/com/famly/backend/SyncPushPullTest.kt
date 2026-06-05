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

class SyncPushPullTest {
    @Test
    fun pushRejectsStaleEntityAndReturnsRejectedList() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_reject_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"reject@famly.app","password":"secret1","displayName":"User"}""")
        }
        val token = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"reject@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"name":"Sync Test"}""")
        }

        val t1 = 1_000_000L
        val t2 = 2_000_000L
        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-1",
                    "payload":"{\"id\":\"tx-1\",\"amountKopecks\":2500,\"type\":\"expense\",\"categoryId\":\"c1\",\"accountId\":\"a_main\",\"dateEpochDay\":20609,\"updatedAt\":$t1,\"createdAt\":$t1}",
                    "syncVersion":1,
                    "updatedAt":$t1,
                    "deleted":false
                  }]
                }""",
            )
        }

        val stalePush = client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-1",
                    "payload":"{\"id\":\"tx-1\",\"amountKopecks\":100,\"type\":\"expense\",\"categoryId\":\"c1\",\"accountId\":\"a1\",\"dateEpochDay\":20000,\"updatedAt\":$t1,\"createdAt\":$t1}",
                    "syncVersion":1,
                    "updatedAt":500000,
                    "deleted":false
                  }]
                }""",
            )
        }
        assertEquals(HttpStatusCode.OK, stalePush.status)
        assertTrue(stalePush.bodyAsText().contains("rejected"))
        assertTrue(stalePush.bodyAsText().contains("transaction:tx-1"))

        val freshPush = client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-1",
                    "payload":"{\"id\":\"tx-1\",\"amountKopecks\":2500,\"type\":\"expense\",\"categoryId\":\"c1\",\"accountId\":\"a_main\",\"dateEpochDay\":20609,\"updatedAt\":$t2,\"createdAt\":$t2}",
                    "syncVersion":1,
                    "updatedAt":$t2,
                    "deleted":false
                  }]
                }""",
            )
        }
        assertTrue(freshPush.bodyAsText().contains("accepted"))
    }

    @Test
    fun pullReturnsEntityAtExactSinceTimestamp() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_since_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"since@famly.app","password":"secret1","displayName":"User"}""")
        }
        val token = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"since@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"name":"Sync Test"}""")
        }

        val updatedAt = System.currentTimeMillis()
        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-exact",
                    "payload":"{\"id\":\"tx-exact\",\"amountKopecks\":2500,\"type\":\"expense\",\"categoryId\":\"c1\",\"accountId\":\"a_main\",\"dateEpochDay\":20609,\"updatedAt\":$updatedAt,\"createdAt\":$updatedAt}",
                    "syncVersion":1,
                    "updatedAt":$updatedAt,
                    "deleted":false
                  }]
                }""",
            )
        }

        val pullBody = client.get("/sync/pull?since=$updatedAt") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        assertTrue(
            pullBody.contains("tx-exact"),
            "Entity with updatedAt equal to since must be included (greaterEq)",
        )
    }

    @Test
    fun nonAdminCannotPromoteToAdmin() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_role_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin3@famly.app","password":"secret1","displayName":"Admin"}""")
        }
        val adminToken = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"admin3@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"name":"Sync Test"}""")
        }
        val householdBody = client.get("/households/mine") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }.bodyAsText()
        val householdId = Regex(""""id":"([^"]+)"""").find(householdBody)!!.groupValues[1]
        val invite = Regex(""""inviteCode":"([^"]+)"""").find(
            client.post("/households/$householdId/invite") {
                header(HttpHeaders.Authorization, "Bearer $adminToken")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"member3@famly.app","password":"secret1","displayName":"Member"}""")
        }
        val memberToken = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"member3@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households/join") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""{"inviteCode":"$invite"}""")
        }
        val memberHousehold = client.get("/households/mine") {
            header(HttpHeaders.Authorization, "Bearer $memberToken")
        }.bodyAsText()
        val memberId = Regex(""""members":\[\{"id":"([^"]+)"""").find(memberHousehold)!!.groupValues[1]

        val response = client.patch("/households/$householdId/members/$memberId") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""{"role":"admin"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
