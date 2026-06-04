package com.famly.backend.plugins

import com.famly.backend.db.HouseholdMembers
import com.famly.backend.db.Households
import com.famly.backend.db.Subscriptions
import com.famly.backend.db.SyncLog
import com.famly.backend.db.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:h2:./build/famly;DB_CLOSE_DELAY=-1"
    val driver = when {
        databaseUrl.startsWith("jdbc:postgresql") -> "org.postgresql.Driver"
        else -> "org.h2.Driver"
    }
    Database.connect(databaseUrl, driver = driver)
    transaction {
        SchemaUtils.create(Users, Households, HouseholdMembers, SyncLog, Subscriptions)
    }
}
