package com.famly.backend

object FamlyConfig {
    val monetizationEnabled: Boolean =
        System.getenv("MONETIZATION_ENABLED")?.equals("true", ignoreCase = true) == true
}
