package com.famly.app.data.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.famly.app.TestFamlyApplication
import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.PendingSyncEntity
import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.data.remote.FamlyApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.LinkedBlockingQueue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = TestFamlyApplication::class)
class SyncRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: FamlyDatabase
    private lateinit var preferences: UserPreferences
    private lateinit var server: MockWebServer
    private val responseQueue = LinkedBlockingQueue<String>()

    @Before
    fun setUp() {
        responseQueue.clear()
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FamlyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferences = UserPreferences(context)
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = responseQueue.poll() ?: """{"error":"unexpected ${request.path}"}"""
                return MockResponse().setResponseCode(200).setBody(body)
            }
        }
        server.start()
        runBlocking {
            preferences.setAuthSession("test-token", "user-1")
            preferences.setHouseholdId("hh-1")
            preferences.setHouseholdName("Test Family")
            preferences.applySyncCursorFix()
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
        db.close()
    }

    @Test
    fun sync_doesNotOverwriteNewerLocalTransaction() = runBlocking {
        val now = System.currentTimeMillis()
        db.transactionDao().upsert(
            TransactionEntity(
                id = "tx-1",
                amountKopecks = 5000,
                type = "expense",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = 20_000,
                note = null,
                createdAt = now,
                updatedAt = 2000,
            ),
        )

        enqueuePull(
            entities = listOf(transactionEntity("tx-1", amount = 1000, updatedAt = 1000)),
            syncToken = 3000,
        )

        val status = repository().sync()
        assertTrue(status.error ?: "sync failed", status.success)

        val tx = db.transactionDao().observeById("tx-1").first()!!
        assertEquals(5000, tx.amountKopecks)
        assertEquals(2000, tx.updatedAt)
    }

    @Test
    fun sync_remoteDeleteReversesAccountBalance() = runBlocking {
        val now = System.currentTimeMillis()
        db.accountDao().upsert(
            AccountEntity(
                id = "a1",
                name = "Main",
                icon = "💳",
                balanceKopecks = 0,
                color = "#000",
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "tx-income",
                amountKopecks = 10_000,
                type = "income",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = 20_000,
                note = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "tx-del",
                amountKopecks = 2500,
                type = "expense",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = 20_001,
                note = null,
                createdAt = now,
                updatedAt = now,
            ),
        )

        enqueuePull(
            entities = listOf(deletedTransactionEntity("tx-del")),
            syncToken = now + 1000,
        )

        val status = repository().sync()
        assertTrue(status.error ?: "sync failed", status.success)

        assertNull(db.transactionDao().observeById("tx-del").first())
        assertEquals(10_000, db.accountDao().getById("a1")!!.balanceKopecks)
    }

    @Test
    fun sync_keepsRejectedPendingEntities() = runBlocking {
        val now = System.currentTimeMillis()
        db.pendingSyncDao().upsert(
            PendingSyncEntity(
                compositeKey = "transaction:tx-pending",
                type = "transaction",
                entityId = "tx-pending",
                payload = """{"id":"tx-pending"}""",
                syncVersion = 1,
                updatedAt = now,
                deleted = false,
            ),
        )

        responseQueue.offer("""{"accepted":[],"rejected":["transaction:tx-pending"]}""")
        enqueuePull(syncToken = now + 500)

        val status = repository().sync()
        assertTrue(status.error ?: "sync failed", status.success)

        val pending = db.pendingSyncDao().getAll()
        assertEquals(1, pending.size)
        assertEquals("transaction:tx-pending", pending.first().compositeKey)
    }

    @Test
    fun sync_emptyPullDoesNotAdvanceCursor() = runBlocking {
        runBlocking { preferences.setLastSyncToken(5000) }

        enqueuePull(entities = emptyList(), syncToken = 9000)

        val status = repository().sync()
        assertTrue(status.error ?: "sync failed", status.success)

        val settings = preferences.settings.first()
        assertEquals(5000L, settings.lastSyncToken)
    }

    @Test
    fun joinHousehold_wipesLocalBudgetDataBeforeSync() = runBlocking {
        val now = System.currentTimeMillis()
        db.categoryDao().upsert(
            com.famly.app.data.local.entity.CategoryEntity(
                id = "c-seed",
                name = "Seed",
                icon = "🍎",
                type = "expense",
                color = "#f00",
                budgetLimitKopecks = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "local-tx",
                amountKopecks = 100,
                type = "expense",
                categoryId = "c-seed",
                accountId = "a1",
                dateEpochDay = 20_000,
                note = null,
                createdAt = now,
                updatedAt = now,
            ),
        )

        responseQueue.offer(
            JSONObject().apply {
                put("id", "hh-joined")
                put("name", "Joined Family")
                put("ownerId", "owner-1")
            }.toString(),
        )
        enqueuePull(
            entities = listOf(
                transactionEntity("server-tx", amount = 3000, updatedAt = now),
            ),
            syncToken = now + 1000,
            householdId = "hh-joined",
        )

        val status = repository().joinHousehold("INVITE1")
        assertTrue(status.error ?: "join failed", status.success)
        assertTrue(db.categoryDao().observeAll().first().isEmpty())
        assertNull(db.transactionDao().observeById("local-tx").first())
        assertEquals(3000L, db.transactionDao().observeById("server-tx").first()?.amountKopecks)
    }

    @Test
    fun reconcileAccountBalances_fixesStaleBalanceFromTransactions() = runBlocking {
        val now = System.currentTimeMillis()
        db.accountDao().upsert(
            AccountEntity(
                id = "a1",
                name = "Наличные",
                icon = "💵",
                balanceKopecks = 0,
                color = "#2D6A4F",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "tx-income",
                amountKopecks = 10_000,
                type = "income",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = 20_000,
                note = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "tx-expense",
                amountKopecks = 2_500,
                type = "expense",
                categoryId = "c1",
                accountId = "a1",
                dateEpochDay = 20_001,
                note = null,
                createdAt = now,
                updatedAt = now,
            ),
        )

        repository().reconcileAccountBalances()

        assertEquals(7_500, db.accountDao().getById("a1")!!.balanceKopecks)
    }

    private fun repository(): SyncRepository {
        val baseUrl = server.url("/").toString().removeSuffix("/")
        return SyncRepository(FamlyApiClient(baseUrl), db, preferences).also {
            it.setOnScheduleSync { }
        }
    }

    private fun enqueuePull(
        entities: List<JSONObject> = emptyList(),
        syncToken: Long = System.currentTimeMillis(),
        householdId: String = "hh-1",
    ) {
        val entitiesArray = JSONArray()
        entities.forEach { entitiesArray.put(it) }
        responseQueue.offer(
            JSONObject().apply {
                put("entities", entitiesArray)
                put("syncToken", syncToken)
                put(
                    "household",
                    JSONObject().apply {
                        put("id", householdId)
                        put("name", "Test Family")
                        put("members", JSONArray())
                    },
                )
            }.toString(),
        )
    }

    private fun transactionEntity(id: String, amount: Long, updatedAt: Long): JSONObject {
        val payload = JSONObject().apply {
            put("id", id)
            put("amountKopecks", amount)
            put("type", "expense")
            put("categoryId", "c1")
            put("accountId", "a1")
            put("dateEpochDay", 20_000)
            put("createdAt", updatedAt)
            put("updatedAt", updatedAt)
        }
        return JSONObject().apply {
            put("type", "transaction")
            put("id", id)
            put("payload", payload.toString())
            put("syncVersion", 1)
            put("updatedAt", updatedAt)
            put("deleted", false)
        }
    }

    private fun deletedTransactionEntity(id: String): JSONObject = JSONObject().apply {
        put("type", "transaction")
        put("id", id)
        put("payload", "{}")
        put("syncVersion", 1)
        put("updatedAt", System.currentTimeMillis())
        put("deleted", true)
    }

    @Test
    fun sync_savingsGoalAndLedgerRoundTrip() = runBlocking {
        val now = System.currentTimeMillis()
        val goalId = "household:hh-1"
        val goalPayload = JSONObject().apply {
            put("id", goalId)
            put("householdId", "hh-1")
            put("goalType", "car")
            put("targetKopecks", 200_000_00)
            put("savedKopecks", 5000)
            put("incomePercent", 10)
            put("isActive", true)
            put("createdAt", now)
            put("updatedAt", now)
        }
        val entryPayload = JSONObject().apply {
            put("id", "entry-1")
            put("goalId", goalId)
            put("amountKopecks", 5000)
            put("entryType", "manual_add")
            put("dateEpochDay", 20_000L)
            put("createdAt", now)
            put("updatedAt", now)
        }
        enqueuePull(
            entities = listOf(
                JSONObject().apply {
                    put("type", "savings_goal")
                    put("id", goalId)
                    put("payload", goalPayload.toString())
                    put("syncVersion", 1)
                    put("updatedAt", now)
                    put("deleted", false)
                },
                JSONObject().apply {
                    put("type", "savings_entry")
                    put("id", "entry-1")
                    put("payload", entryPayload.toString())
                    put("syncVersion", 1)
                    put("updatedAt", now)
                    put("deleted", false)
                },
            ),
        )
        responseQueue.offer("""{"accepted":[]}""")
        val repo = repository()
        val status = repo.sync()
        assertTrue(status.success)
        val goal = db.savingsGoalDao().getById(goalId)
        assertEquals("car", goal?.goalType)
        assertEquals(5000L, goal?.savedKopecks)
        val entry = db.savingsLedgerDao().getById("entry-1")
        assertEquals("manual_add", entry?.entryType)
    }
}
