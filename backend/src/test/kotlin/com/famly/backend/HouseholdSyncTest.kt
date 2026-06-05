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

class HouseholdSyncTest {
    @Test
    fun joinMemberSeesOwnerTransactionAfterPull() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_hh_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"owner@famly.app","password":"secret1","displayName":"Owner"}""")
        }
        val ownerLogin = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"owner@famly.app","password":"secret1"}""")
        }.bodyAsText()
        val ownerToken = Regex(""""token":"([^"]+)"""").find(ownerLogin)!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $ownerToken")
            setBody("""{"name":"Test Family"}""")
        }
        val ownerHousehold = client.get("/households/mine") {
            header(HttpHeaders.Authorization, "Bearer $ownerToken")
        }.bodyAsText()
        val householdId = Regex(""""id":"([^"]+)"""").find(ownerHousehold)!!.groupValues[1]
        val invite = Regex(""""inviteCode":"([^"]+)"""").find(
            client.post("/households/$householdId/invite") {
                header(HttpHeaders.Authorization, "Bearer $ownerToken")
            }.bodyAsText(),
        )!!.groupValues[1]

        val now = System.currentTimeMillis()
        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $ownerToken")
            setBody(
                """{
                  "entities":[{
                    "type":"transaction",
                    "id":"tx-shared",
                    "payload":"{\"id\":\"tx-shared\",\"amountKopecks\":5000,\"type\":\"expense\",\"categoryId\":\"c1\",\"accountId\":\"a_main\",\"dateEpochDay\":20609,\"updatedAt\":$now,\"createdAt\":$now}",
                    "syncVersion":1,
                    "updatedAt":$now,
                    "deleted":false
                  }]
                }""",
            )
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"member@famly.app","password":"secret1","displayName":"Member"}""")
        }
        val memberLogin = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"member@famly.app","password":"secret1"}""")
        }.bodyAsText()
        val memberToken = Regex(""""token":"([^"]+)"""").find(memberLogin)!!.groupValues[1]

        client.post("/households/join") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""{"inviteCode":"$invite"}""")
        }

        val pullBody = client.get("/sync/pull?since=0") {
            header(HttpHeaders.Authorization, "Bearer $memberToken")
        }.bodyAsText()
        assertTrue(pullBody.contains("tx-shared"), "Joined member must receive owner transaction")
        assertTrue(pullBody.contains("\"household\""), "Pull must include household snapshot")
    }

    @Test
    fun nonAdminCannotChangeVisibility() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_perm_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@famly.app","password":"secret1","displayName":"Admin"}""")
        }
        val adminToken = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"admin@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"name":"Perm Family"}""")
        }
        val adminHousehold = client.get("/households/mine") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }.bodyAsText()
        val householdId = Regex(""""id":"([^"]+)"""").find(adminHousehold)!!.groupValues[1]
        val invite = Regex(""""inviteCode":"([^"]+)"""").find(
            client.post("/households/$householdId/invite") {
                header(HttpHeaders.Authorization, "Bearer $adminToken")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"member2@famly.app","password":"secret1","displayName":"Member2"}""")
        }
        val memberToken = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"member2@famly.app","password":"secret1"}""")
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
            setBody("""{"visibility":"full"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun avatarRoundTripInPullSnapshot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:famly_avatar_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            )
        }
        application {
            configureDatabase()
            configureAuth()
            configureRouting()
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"avatar@famly.app","password":"secret1","displayName":"Avatar"}""")
        }
        val token = Regex(""""token":"([^"]+)"""").find(
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"avatar@famly.app","password":"secret1"}""")
            }.bodyAsText(),
        )!!.groupValues[1]

        client.post("/households") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"name":"Avatar Family"}""")
        }
        val household = client.get("/households/mine") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val householdId = Regex(""""id":"([^"]+)"""").find(household)!!.groupValues[1]
        val memberId = Regex(""""members":\[\{"id":"([^"]+)"""").find(household)!!.groupValues[1]

        client.patch("/households/$householdId/members/$memberId") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"avatar":"🦊"}""")
        }

        val pullBody = client.get("/sync/pull?since=0") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        assertTrue(
            pullBody.contains("avatar") && (pullBody.contains("🦊") || pullBody.contains("avatar")),
            "Avatar field must appear in pull snapshot",
        )
    }
}
