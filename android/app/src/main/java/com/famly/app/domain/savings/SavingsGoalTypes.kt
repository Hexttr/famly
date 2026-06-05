package com.famly.app.domain.savings

enum class SavingsGoalType(val key: String, val label: String, val emoji: String) {
    CAR("car", "Автомобиль", "🚗"),
    APARTMENT("apartment", "Квартира", "🏢"),
    HOUSE("house", "Дом", "🏡"),
    BUSINESS("business", "Бизнес", "💼"),
    VACATION("vacation", "Отпуск", "🏖️"),
    OTHER("other", "Другое", "⭐"),
    ;

    companion object {
        fun fromKey(key: String): SavingsGoalType =
            entries.find { it.key == key } ?: OTHER
    }
}

fun savingsGoalId(householdId: String): String = "household:$householdId"

fun savingsGoalDisplayName(goalType: String, customName: String?): String {
    val type = SavingsGoalType.fromKey(goalType)
    return if (type == SavingsGoalType.OTHER && !customName.isNullOrBlank()) {
        customName.trim()
    } else {
        type.label
    }
}

const val SAVINGS_GOAL_ACCOUNT_PREFIX = "savings_goal:"

fun savingsGoalAccountId(goalId: String): String = "$SAVINGS_GOAL_ACCOUNT_PREFIX$goalId"

fun isSavingsGoalAccountId(accountId: String): Boolean =
    accountId.startsWith(SAVINGS_GOAL_ACCOUNT_PREFIX)
