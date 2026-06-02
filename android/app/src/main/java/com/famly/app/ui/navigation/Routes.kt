package com.famly.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val OPERATIONS = "operations"
    const val OPERATION_DETAIL = "operations/{id}"
    const val BUDGET = "budget"
    const val BUDGET_CATEGORY = "budget/{id}"
    const val CATEGORIES = "categories"
    const val MORE = "more"
    const val ACCOUNTS = "accounts"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val PREMIUM = "premium"
    const val FAMILY = "family"
    const val FAMILY_MEMBER = "family/{id}"
    const val BALANCES = "balances"
    const val ANALYTICS = "analytics"
    const val SPLIT = "split/{id}"

    fun operationDetail(id: String) = "operations/$id"
    fun budgetCategory(id: String) = "budget/$id"
    fun familyMember(id: String) = "family/$id"
    fun split(id: String) = "split/$id"
}
