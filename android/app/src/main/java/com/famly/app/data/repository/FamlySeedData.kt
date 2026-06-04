package com.famly.app.data.repository

import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity

internal object FamlySeedData {
    const val DEFAULT_ACCOUNT_ID = "a_main"

    val LEGACY_DEMO_TX_IDS: List<String> = (1..41).map { "t$it" }
    val LEGACY_DEMO_ACCOUNT_IDS: List<String> = listOf("a1", "a2", "a3")

    fun defaultAccount(now: Long): AccountEntity =
        AccountEntity(
            id = DEFAULT_ACCOUNT_ID,
            name = "Наличные",
            icon = "💵",
            balanceKopecks = 0,
            color = "#2D6A4F",
            sortOrder = 0,
            createdAt = now,
            updatedAt = now,
        )

    val LEGACY_SEED_BUDGET_LIMITS: Map<String, Long> = mapOf(
        "c1" to 2_500_000L,
        "c2" to 800_000L,
        "c3" to 600_000L,
        "c4" to 1_200_000L,
        "c5" to 500_000L,
        "c8" to 150_000L,
    )

    fun categories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity("c1", "Продукты", "🛒", "expense", "#E63946", 0L, 0, false, 0, now, now),
        CategoryEntity("c2", "Транспорт", "🚌", "expense", "#457B9D", 0L, 0, false, 1, now, now),
        CategoryEntity("c3", "Кафе", "☕", "expense", "#F4A261", 0L, 0, false, 2, now, now),
        CategoryEntity("c4", "ЖКХ", "🏠", "expense", "#6D597A", 0L, 0, false, 3, now, now),
        CategoryEntity("c5", "Развлечения", "🎬", "expense", "#E76F51", 0L, 0, false, 4, now, now),
        CategoryEntity("c6", "Зарплата", "💰", "income", "#2D6A4F", null, 0, false, 5, now, now),
        CategoryEntity("c7", "Фриланс", "💻", "income", "#40916C", null, 0, false, 6, now, now),
        CategoryEntity("c8", "Подписки", "📺", "expense", "#7209B7", 0L, 0, false, 7, now, now),
    )
}
