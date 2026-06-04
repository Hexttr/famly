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

    fun categories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity("c1", "Продукты", "🛒", "expense", "#E63946", 2_500_000, 0, false, 0, now, now),
        CategoryEntity("c2", "Транспорт", "🚌", "expense", "#457B9D", 800_000, 0, false, 1, now, now),
        CategoryEntity("c3", "Кафе", "☕", "expense", "#F4A261", 600_000, 0, false, 2, now, now),
        CategoryEntity("c4", "ЖКХ", "🏠", "expense", "#6D597A", 1_200_000, 0, false, 3, now, now),
        CategoryEntity("c5", "Развлечения", "🎬", "expense", "#E76F51", 500_000, 0, false, 4, now, now),
        CategoryEntity("c6", "Зарплата", "💰", "income", "#2D6A4F", null, 0, false, 5, now, now),
        CategoryEntity("c7", "Фриланс", "💻", "income", "#40916C", null, 0, false, 6, now, now),
        CategoryEntity("c8", "Подписки", "📺", "expense", "#7209B7", 150_000, 0, false, 7, now, now),
    )
}
