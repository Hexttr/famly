package com.famly.backend

object FamlyConfig {
    val monetizationEnabled: Boolean =
        System.getenv("MONETIZATION_ENABLED")?.equals("true", ignoreCase = true) == true

    val publicBaseUrl: String =
        System.getenv("PUBLIC_BASE_URL")?.trimEnd('/') ?: "https://api.jazz68.ru"
}
