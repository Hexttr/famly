package com.famly.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.famly.app.domain.model.AppSettings
import com.famly.app.domain.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "famly_settings")

class UserPreferences(private val context: Context) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[KEY_THEME] ?: "light",
            budgetPeriod = BudgetPeriod(
                startDay = prefs[KEY_START_DAY] ?: 28,
                type = prefs[KEY_PERIOD_TYPE] ?: "monthly",
            ),
            currency = prefs[KEY_CURRENCY] ?: "RUB",
            onboardingComplete = prefs[KEY_ONBOARDING] ?: false,
            isPremium = prefs[KEY_PREMIUM] ?: false,
            trialEndsAt = prefs[KEY_TRIAL_END]?.takeIf { it > 0 },
            premiumExpiresAt = prefs[KEY_PREMIUM_EXPIRES]?.takeIf { it > 0 },
            authToken = prefs[KEY_AUTH_TOKEN],
            userId = prefs[KEY_USER_ID],
            householdId = prefs[KEY_HOUSEHOLD_ID],
            householdName = prefs[KEY_HOUSEHOLD_NAME],
            lastSyncToken = prefs[KEY_LAST_SYNC_TOKEN]?.takeIf { it > 0 },
            lastRolloverPeriodStart = prefs[KEY_LAST_ROLLOVER_PERIOD]?.takeIf { it != 0L },
            dismissedNotificationIds = prefs[KEY_DISMISSED_NOTIFICATIONS] ?: emptySet(),
        )
    }

    suspend fun setOnboardingComplete() = context.dataStore.edit { it[KEY_ONBOARDING] = true }

    suspend fun setTheme(theme: String) = context.dataStore.edit { it[KEY_THEME] = theme }

    suspend fun setBudgetStartDay(day: Int) = context.dataStore.edit { it[KEY_START_DAY] = day }

    suspend fun setCurrency(currency: String) = context.dataStore.edit { it[KEY_CURRENCY] = currency }

    suspend fun activatePremium() = context.dataStore.edit {
        it[KEY_PREMIUM] = true
        it[KEY_TRIAL_END] = 0L
    }

    suspend fun initTrialIfNeeded() = context.dataStore.edit { prefs ->
        if (!prefs.contains(KEY_TRIAL_END) && prefs[KEY_PREMIUM] != true) {
            prefs[KEY_TRIAL_END] = System.currentTimeMillis() + TRIAL_MS
        }
    }

    suspend fun setAuthSession(token: String, userId: String) = context.dataStore.edit {
        it[KEY_AUTH_TOKEN] = token
        it[KEY_USER_ID] = userId
    }

    suspend fun clearAuthSession() = context.dataStore.edit {
        it.remove(KEY_AUTH_TOKEN)
        it.remove(KEY_USER_ID)
        it.remove(KEY_HOUSEHOLD_ID)
        it.remove(KEY_LAST_SYNC_TOKEN)
    }

    suspend fun setHouseholdId(householdId: String) = context.dataStore.edit {
        it[KEY_HOUSEHOLD_ID] = householdId
    }

    suspend fun setHouseholdName(name: String) = context.dataStore.edit {
        it[KEY_HOUSEHOLD_NAME] = name.trim()
    }

    suspend fun isSeedBudgetZeroed(): Boolean =
        context.dataStore.data.first()[KEY_SEED_BUDGET_ZEROED] == true

    suspend fun setSeedBudgetZeroed() = context.dataStore.edit {
        it[KEY_SEED_BUDGET_ZEROED] = true
    }

    suspend fun setLastSyncToken(token: Long) = context.dataStore.edit {
        it[KEY_LAST_SYNC_TOKEN] = token
    }

    suspend fun setLastRolloverPeriodStart(epochDay: Long) = context.dataStore.edit {
        it[KEY_LAST_ROLLOVER_PERIOD] = epochDay
    }

    suspend fun isLegacyDemoPurged(): Boolean =
        context.dataStore.data.first()[KEY_LEGACY_DEMO_PURGED] == true

    suspend fun setLegacyDemoPurged() = context.dataStore.edit {
        it[KEY_LEGACY_DEMO_PURGED] = true
    }

    suspend fun dismissNotification(id: String) = context.dataStore.edit { prefs ->
        val current = prefs[KEY_DISMISSED_NOTIFICATIONS] ?: emptySet()
        prefs[KEY_DISMISSED_NOTIFICATIONS] = current + id
    }

    suspend fun clearDismissedNotifications() = context.dataStore.edit {
        it.remove(KEY_DISMISSED_NOTIFICATIONS)
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_START_DAY = intPreferencesKey("start_day")
        private val KEY_PERIOD_TYPE = stringPreferencesKey("period_type")
        private val KEY_CURRENCY = stringPreferencesKey("currency")
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
        private val KEY_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_TRIAL_END = longPreferencesKey("trial_ends_at")
        private val KEY_PREMIUM_EXPIRES = longPreferencesKey("premium_expires_at")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_HOUSEHOLD_ID = stringPreferencesKey("household_id")
        private val KEY_HOUSEHOLD_NAME = stringPreferencesKey("household_name")
        private val KEY_SEED_BUDGET_ZEROED = booleanPreferencesKey("seed_budget_zeroed_v2")
        private val KEY_LAST_SYNC_TOKEN = longPreferencesKey("last_sync_token")
        private val KEY_LAST_ROLLOVER_PERIOD = longPreferencesKey("last_rollover_period_start")
        private val KEY_LEGACY_DEMO_PURGED = booleanPreferencesKey("legacy_demo_purged_v1")
        private val KEY_DISMISSED_NOTIFICATIONS = stringSetPreferencesKey("dismissed_notifications")
        private const val TRIAL_MS = 7L * 24 * 60 * 60 * 1000
    }
}
