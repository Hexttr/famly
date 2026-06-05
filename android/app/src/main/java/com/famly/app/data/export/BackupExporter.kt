package com.famly.app.data.export

import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.FamilyMemberEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.model.AppSettings
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object BackupExporter {

    fun export(
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        familyMembers: List<FamilyMemberEntity>,
        settings: AppSettings,
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", Instant.now().toString())
        root.put("accounts", JSONArray(accounts.map { it.toJson() }))
        root.put("categories", JSONArray(categories.map { it.toJson() }))
        root.put("transactions", JSONArray(transactions.map { it.toJson() }))
        root.put("familyMembers", JSONArray(familyMembers.map { it.toJson() }))
        root.put("settings", settings.toJson())
        return root.toString(2)
    }

    private fun AccountEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("icon", icon)
        put("balanceKopecks", balanceKopecks)
        put("color", color)
        put("sortOrder", sortOrder)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun CategoryEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("icon", icon)
        put("type", type)
        put("color", color)
        put("budgetLimitKopecks", budgetLimitKopecks)
        put("rolloverKopecks", rolloverKopecks)
        put("sortOrder", sortOrder)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun TransactionEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("amountKopecks", amountKopecks)
        put("type", type)
        put("categoryId", categoryId)
        put("accountId", accountId)
        put("dateEpochDay", dateEpochDay)
        put("note", note)
        put("isRecurring", isRecurring)
        put("recurringDay", recurringDay)
        put("lastRecurrenceEpochDay", lastRecurrenceEpochDay)
        put("isPrivate", isPrivate)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun FamilyMemberEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("householdId", householdId)
        put("name", name)
        put("role", role)
        put("visibility", visibility)
        put("avatar", avatar)
        put("syncVersion", syncVersion)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun AppSettings.toJson() = JSONObject().apply {
        put("theme", theme)
        put("budgetPeriod", JSONObject().apply {
            put("startDay", budgetPeriod.startDay)
            put("type", budgetPeriod.type)
        })
        put("currency", currency)
        put("onboardingComplete", onboardingComplete)
        put("isPremium", isPremium)
        put("trialEndsAt", trialEndsAt)
        put("premiumExpiresAt", premiumExpiresAt)
    }
}
