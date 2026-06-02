package com.famly.backend.plugins

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.famly.backend.db.*
import io.ktor.server.application.*

fun Application.configureDatabase() {
    Database.connect("jdbc:h2:./build/famly;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(Users, Households, HouseholdMembers, SyncLog, Subscriptions)
    }
}
