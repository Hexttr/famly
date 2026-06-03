package com.famly.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.famly.app.domain.model.AppSettings
import com.famly.app.domain.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow
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
            lastSyncToken = prefs[KEY_LAST_SYNC_TOKEN]?.takeIf { it > 0 },
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

    suspend fun setLastSyncToken(token: Long) = context.dataStore.edit {
        it[KEY_LAST_SYNC_TOKEN] = token
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
        private val KEY_LAST_SYNC_TOKEN = longPreferencesKey("last_sync_token")
        private const val TRIAL_MS = 7L * 24 * 60 * 60 * 1000
    }
}
