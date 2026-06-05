package com.famly.backend.plugins

import com.famly.backend.db.AdminAuditLog
import com.famly.backend.db.HouseholdMembers
import com.famly.backend.db.Households
import com.famly.backend.db.Subscriptions
import com.famly.backend.db.SyncLog
import com.famly.backend.db.Users
import com.famly.backend.services.AuthService
import com.famly.backend.services.seedAdminUser
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val databaseUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: "jdbc:h2:./build/famly;DB_CLOSE_DELAY=-1"
    val driver = when {
        databaseUrl.startsWith("jdbc:postgresql") -> "org.postgresql.Driver"
        else -> "org.h2.Driver"
    }
    Database.connect(databaseUrl, driver = driver)
    transaction {
        SchemaUtils.create(
            Users,
            Households,
            HouseholdMembers,
            SyncLog,
            Subscriptions,
            AdminAuditLog,
        )
        listOf(
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at BIGINT DEFAULT 0",
            "ALTER TABLE households ADD COLUMN IF NOT EXISTS created_at BIGINT DEFAULT 0",
        ).forEach { sql -> runCatching { exec(sql) } }
        listOf(
            "ALTER TABLE sync_log ALTER COLUMN id SET DATA TYPE VARCHAR(128)",
            "ALTER TABLE sync_log ALTER COLUMN entity_id SET DATA TYPE VARCHAR(128)",
            "ALTER TABLE sync_log ALTER COLUMN id VARCHAR(128)",
            "ALTER TABLE sync_log ALTER COLUMN entity_id VARCHAR(128)",
        ).forEach { sql -> runCatching { exec(sql) } }
    }
    seedAdminUser(AuthService())
}
