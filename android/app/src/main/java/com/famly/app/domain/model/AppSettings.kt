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
) {
    fun hasPremiumAccess(now: Long = System.currentTimeMillis()): Boolean =
        isPremium || (trialEndsAt != null && trialEndsAt > now)

    fun trialDaysLeft(now: Long = System.currentTimeMillis()): Int {
        if (isPremium || trialEndsAt == null) return 0
        return maxOf(0, ((trialEndsAt - now) / (24 * 60 * 60 * 1000)).toInt() + 1)
    }
}
