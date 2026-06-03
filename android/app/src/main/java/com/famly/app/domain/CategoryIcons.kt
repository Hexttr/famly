package com.famly.app.domain

data class CategoryIconDef(
    val id: String,
    val emoji: String,
    val label: String,
    val color: String,
    val types: List<String>,
)

val CATEGORY_ICONS: List<CategoryIconDef> = listOf(
    CategoryIconDef("groceries", "🛒", "Продукты", "#E63946", listOf("expense")),
    CategoryIconDef("transport", "🚌", "Транспорт", "#457B9D", listOf("expense")),
    CategoryIconDef("cafe", "☕", "Кафе", "#F4A261", listOf("expense")),
    CategoryIconDef("restaurant", "🍽️", "Ресторан", "#E76F51", listOf("expense")),
    CategoryIconDef("home", "🏠", "ЖКХ", "#6D597A", listOf("expense")),
    CategoryIconDef("rent", "🔑", "Аренда", "#5C4D7D", listOf("expense")),
    CategoryIconDef("utilities", "⚡", "Коммуналка", "#7B6CF6", listOf("expense")),
    CategoryIconDef("entertainment", "🎬", "Развлечения", "#E76F51", listOf("expense")),
    CategoryIconDef("health", "💊", "Здоровье", "#2A9D8F", listOf("expense")),
    CategoryIconDef("clothes", "👕", "Одежда", "#9B5DE5", listOf("expense")),
    CategoryIconDef("gifts", "🎁", "Подарки", "#F72585", listOf("expense")),
    CategoryIconDef("education", "📚", "Обучение", "#4361EE", listOf("expense", "income")),
    CategoryIconDef("kids", "🧒", "Дети", "#FF6B9D", listOf("expense")),
    CategoryIconDef("family", "👨‍👩‍👧", "Семья", "#2D6A4F", listOf("expense", "income")),
    CategoryIconDef("pets", "🐾", "Питомцы", "#BC6C25", listOf("expense")),
    CategoryIconDef("sport", "⚽", "Спорт", "#06AED5", listOf("expense")),
    CategoryIconDef("travel", "✈️", "Путешествия", "#118AB2", listOf("expense")),
    CategoryIconDef("beauty", "💅", "Красота", "#FF85A1", listOf("expense")),
    CategoryIconDef("phone", "📱", "Связь", "#4CC9F0", listOf("expense")),
    CategoryIconDef("subscriptions", "📺", "Подписки", "#7209B7", listOf("expense")),
    CategoryIconDef("car", "🚗", "Авто", "#3A5A40", listOf("expense")),
    CategoryIconDef("fuel", "⛽", "Бензин", "#D4A373", listOf("expense")),
    CategoryIconDef("tax", "📋", "Налоги", "#6C757D", listOf("expense")),
    CategoryIconDef("other", "📦", "Другое", "#8A9390", listOf("expense", "income")),
    CategoryIconDef("salary", "💰", "Зарплата", "#2D6A4F", listOf("income")),
    CategoryIconDef("freelance", "💻", "Фриланс", "#40916C", listOf("income")),
    CategoryIconDef("investment", "📈", "Инвестиции", "#1B4332", listOf("income")),
    CategoryIconDef("savings", "🏦", "Накопления", "#52B788", listOf("income", "expense")),
)

const val DEFAULT_EXPENSE_ICON = "📦"
const val DEFAULT_INCOME_ICON = "💰"

fun getCategoryIconDef(idOrEmoji: String): CategoryIconDef =
    CATEGORY_ICONS.find { it.id == idOrEmoji || it.emoji == idOrEmoji }
        ?: CATEGORY_ICONS.first { it.id == "other" }

fun iconsForType(type: String): List<CategoryIconDef> =
    CATEGORY_ICONS.filter { type in it.types }

fun nextCategoryIcon(current: String, type: String): String {
    val icons = iconsForType(type)
    if (icons.isEmpty()) return if (type == "income") DEFAULT_INCOME_ICON else DEFAULT_EXPENSE_ICON
    val idx = icons.indexOfFirst { it.emoji == current || it.id == current }
    if (idx == -1) return icons.first().emoji
    return icons[(idx + 1) % icons.size].emoji
}
