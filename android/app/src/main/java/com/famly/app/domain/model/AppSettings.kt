package com.famly.app.domain.model

data class BudgetPeriod(
    val startDay: Int,
    val type: String,
)

data class AppSettings(
    val theme: String,
    val budgetPeriod: BudgetPeriod,
    val currency: String,
    val onboardingComplete: Boolean,
    val isPremium: Boolean,
    val trialEndsAt: Long?,
    val premiumExpiresAt: Long?,
    val authToken: String? = null,
    val userId: String? = null,
    val householdId: String? = null,
    val householdName: String? = null,
    val lastSyncToken: Long? = null,
    val lastSyncAttemptAt: Long? = null,
    val lastRolloverPeriodStart: Long? = null,
    val dismissedNotificationIds: Set<String> = emptySet(),
    val pinnedQuickCategoryIds: List<String> = emptyList(),
) {
    val isAuthenticated: Boolean get() = !authToken.isNullOrBlank() && !userId.isNullOrBlank()

    val isSynced: Boolean get() = isAuthenticated && !householdId.isNullOrBlank()
    fun hasPremiumAccess(now: Long = System.currentTimeMillis()): Boolean =
        isPremium || (trialEndsAt != null && trialEndsAt > now)

    fun trialDaysLeft(now: Long = System.currentTimeMillis()): Int {
        if (isPremium || trialEndsAt == null) return 0
        return maxOf(0, ((trialEndsAt - now) / (24 * 60 * 60 * 1000)).toInt() + 1)
    }
}
