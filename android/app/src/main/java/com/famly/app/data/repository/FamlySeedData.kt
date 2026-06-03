package com.famly.app.data.repository

import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.IouBalanceEntity
import com.famly.app.data.local.entity.TransactionEntity
import java.time.LocalDate
import java.util.UUID

internal object FamlySeedData {
    const val HOUSEHOLD_ID = "h1"

    fun accounts(now: Long): List<AccountEntity> = listOf(
        AccountEntity("a1", "Наличные", "💵", 1_250_000, "#52B788", 0, now, now),
        AccountEntity("a2", "Сбербанк", "💳", 8_730_000, "#2D6A4F", 1, now, now),
        AccountEntity("a3", "Накопления", "🏦", 15_000_000, "#40916C", 2, now, now),
    )

    fun categories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity("c1", "Продукты", "🛒", "expense", "#E63946", 2_500_000, 0, 0, now, now),
        CategoryEntity("c2", "Транспорт", "🚌", "expense", "#457B9D", 800_000, 0, 1, now, now),
        CategoryEntity("c3", "Кафе", "☕", "expense", "#F4A261", 600_000, 0, 2, now, now),
        CategoryEntity("c4", "ЖКХ", "🏠", "expense", "#6D597A", 1_200_000, 0, 3, now, now),
        CategoryEntity("c5", "Развлечения", "🎬", "expense", "#E76F51", 500_000, 0, 4, now, now),
        CategoryEntity("c6", "Зарплата", "💰", "income", "#2D6A4F", null, 0, 5, now, now),
        CategoryEntity("c7", "Фриланс", "💻", "income", "#40916C", null, 0, 6, now, now),
        CategoryEntity("c8", "Подписки", "📺", "expense", "#7209B7", 150_000, 0, 7, now, now),
    )

    fun familyMembers(now: Long): List<FamilyMemberEntity> = listOf(
        FamilyMemberEntity("f1", HOUSEHOLD_ID, "Алексей", "admin", "full", "👨", 0, now, now),
        FamilyMemberEntity("f2", HOUSEHOLD_ID, "Мария", "member", "partial", "👩", 0, now, now),
        FamilyMemberEntity("f3", HOUSEHOLD_ID, "Саша", "viewer", "private", "👦", 0, now, now),
        FamilyMemberEntity("f4", HOUSEHOLD_ID, "Дедушка Виктор", "viewer", "partial", "👴", 0, now, now),
        FamilyMemberEntity("f5", HOUSEHOLD_ID, "Бабушка Нина", "viewer", "partial", "👵", 0, now, now),
    )

    fun iouBalances(now: Long): List<IouBalanceEntity> = listOf(
        IouBalanceEntity("iou1", "f2", "f1", 125_000, null, now, now),
        IouBalanceEntity("iou2", "f1", "f2", 80_000, null, now, now),
    )

    fun transactions(now: Long): List<TransactionEntity> {
        fun d(date: String) = LocalDate.parse(date).toEpochDay()
        fun tx(
            id: String,
            rubles: Long,
            type: String,
            categoryId: String,
            accountId: String,
            date: String,
            note: String,
            recurring: Boolean = false,
            recurringDay: Int? = null,
            lastRecurrence: Long? = null,
        ) = TransactionEntity(
            id = id,
            amountKopecks = rubles * 100,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            dateEpochDay = d(date),
            note = note,
            isRecurring = recurring,
            recurringDay = recurringDay,
            lastRecurrenceEpochDay = lastRecurrence,
            createdAt = now,
            updatedAt = now,
        )
        return listOf(
            tx("t1", 1847, "expense", "c1", "a2", "2026-06-02", "Пятёрочка"),
            tx("t2", 89, "expense", "c2", "a2", "2026-06-02", "Метро"),
            tx("t3", 450, "expense", "c3", "a1", "2026-06-01", "Кофейня"),
            tx("t4", 85000, "income", "c6", "a2", "2026-05-28", "Зарплата май"),
            tx("t5", 3200, "expense", "c4", "a2", "2026-05-30", "Электричество"),
            tx("t6", 1200, "expense", "c5", "a2", "2026-05-29", "Кино"),
            tx("t7", 2100, "expense", "c1", "a2", "2026-05-28", "Магнит"),
            tx("t8", 15000, "income", "c7", "a2", "2026-05-25", "Проект"),
            tx("t9", 650, "expense", "c1", "a2", "2026-05-27", "ВкусВилл"),
            tx("t10", 120, "expense", "c2", "a2", "2026-05-27", "Автобус"),
            tx("t11", 890, "expense", "c3", "a1", "2026-05-26", "Обед"),
            tx("t12", 4500, "expense", "c4", "a2", "2026-05-26", "Вода и газ"),
            tx("t13", 780, "expense", "c5", "a2", "2026-05-25", "Стрижка"),
            tx("t14", 2340, "expense", "c1", "a2", "2026-05-24", "Лента"),
            tx("t15", 350, "expense", "c2", "a2", "2026-05-24", "Яндекс Go"),
            tx("t16", 520, "expense", "c3", "a1", "2026-05-23", "Starbucks"),
            tx("t17", 1800, "expense", "c5", "a2", "2026-05-22", "Концерт"),
            tx("t18", 980, "expense", "c1", "a2", "2026-05-21", "Перекрёсток"),
            tx("t19", 65, "expense", "c2", "a2", "2026-05-21", "Метро"),
            tx("t20", 1100, "expense", "c4", "a2", "2026-05-20", "Интернет"),
            tx("t21", 340, "expense", "c3", "a1", "2026-05-19", "Кофе с собой"),
            tx("t22", 1560, "expense", "c1", "a2", "2026-05-18", "Ашан"),
            tx("t23", 2400, "expense", "c5", "a2", "2026-05-17", "Ресторан"),
            tx("t24", 430, "expense", "c2", "a2", "2026-05-16", "Каршеринг"),
            tx("t25", 720, "expense", "c3", "a1", "2026-05-15", "Пекарня"),
            tx("t26", 8900, "expense", "c4", "a2", "2026-05-14", "Аренда"),
            tx("t27", 1120, "expense", "c1", "a2", "2026-05-13", "Дикси"),
            tx("t28", 280, "expense", "c2", "a2", "2026-05-12", "Трамвай"),
            tx("t29", 670, "expense", "c3", "a1", "2026-05-11", "Суши"),
            tx("t30", 950, "expense", "c5", "a2", "2026-05-10", "Боулинг"),
            tx("t31", 1890, "expense", "c1", "a2", "2026-05-09", "Магнит"),
            tx("t32", 150, "expense", "c2", "a2", "2026-05-08", "Маршрутка"),
            tx("t33", 410, "expense", "c3", "a1", "2026-05-07", "Чайная"),
            tx("t34", 5600, "expense", "c4", "a2", "2026-05-06", "Капремонт"),
            tx("t35", 1350, "expense", "c5", "a2", "2026-05-05", "Театр"),
            tx("t36", 760, "expense", "c1", "a2", "2026-05-04", "Пятёрочка"),
            tx("t37", 990, "expense", "c3", "a1", "2026-05-03", "Пицца"),
            tx("t38", 220, "expense", "c2", "a2", "2026-05-02", "Метро"),
            tx(
                "t39",
                8900,
                "expense",
                "c4",
                "a2",
                "2026-05-14",
                "Аренда",
                recurring = true,
                recurringDay = 14,
                lastRecurrence = d("2026-05-14"),
            ),
            tx(
                "t40",
                599,
                "expense",
                "c8",
                "a2",
                "2026-05-15",
                "Netflix",
                recurring = true,
                recurringDay = 15,
                lastRecurrence = d("2026-05-15"),
            ),
            tx(
                "t41",
                3200,
                "expense",
                "c4",
                "a2",
                "2026-05-05",
                "ЖКХ",
                recurring = true,
                recurringDay = 5,
                lastRecurrence = d("2026-05-05"),
            ),
        )
    }
}
