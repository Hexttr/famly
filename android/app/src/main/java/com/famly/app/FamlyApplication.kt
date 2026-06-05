package com.famly.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.famly.app.billing.RuStoreBillingManager
import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.remote.FamlyApiClient
import com.famly.app.data.repository.FamlyRepository
import com.famly.app.data.sync.SyncRepository
import com.famly.app.data.work.BudgetRolloverWorkScheduler
import com.famly.app.data.work.RecurringWorkScheduler
import com.famly.app.data.work.SyncWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FamlyApplication : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: FamlyRepository
        private set

    lateinit var syncRepository: SyncRepository
        private set

    lateinit var billingManager: RuStoreBillingManager
        private set

    private lateinit var preferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        val db = FamlyDatabase.get(this)
        preferences = UserPreferences(this)
        syncRepository = SyncRepository(FamlyApiClient(), db, preferences)
        syncRepository.setOnScheduleSync { SyncWorkScheduler.scheduleDebounced(this) }
        repository = FamlyRepository(db, preferences, syncRepository)
        billingManager = RuStoreBillingManager(
            onPremiumActivated = { repository.activatePremium() },
            syncRepository = syncRepository,
        )
        RecurringWorkScheduler.schedule(this)
        SyncWorkScheduler.schedulePeriodic(this)
        BudgetRolloverWorkScheduler.schedule(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    appScope.launch {
                        val settings = preferences.settings.first()
                        if (settings.authToken.isNullOrBlank() || settings.householdId.isNullOrBlank()) return@launch
                        SyncWorkScheduler.scheduleOnForeground(
                            this@FamlyApplication,
                            preferences.getLastSyncAttemptAt(),
                        )
                    }
                }
            },
        )
        appScope.launch {
            repository.ensureSeeded()
            repository.processDueRecurring()
            repository.processBudgetRollover()
            val settings = preferences.settings.first()
            if (!settings.authToken.isNullOrBlank() && !settings.householdId.isNullOrBlank()) {
                SyncWorkScheduler.scheduleDebounced(this@FamlyApplication, delaySeconds = 5)
            }
        }
    }
}
