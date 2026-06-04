package com.famly.app.domain

import com.famly.app.BuildConfig
import com.famly.app.domain.model.AppSettings

object FamlyAccess {
    const val FREE_TIER_EXPORT_DAYS = 30

    fun monetizationEnabled(): Boolean = BuildConfig.MONETIZATION_ENABLED

    fun hasPremium(settings: AppSettings, now: Long = System.currentTimeMillis()): Boolean {
        if (!BuildConfig.MONETIZATION_ENABLED) return true
        return settings.hasPremiumAccess(now)
    }

    fun showPaywall(): Boolean = BuildConfig.MONETIZATION_ENABLED

    fun exportDaysLimit(settings: AppSettings, requested: Int?): Int? {
        if (hasPremium(settings)) return requested
        return requested?.coerceAtMost(FREE_TIER_EXPORT_DAYS) ?: FREE_TIER_EXPORT_DAYS
    }
}
