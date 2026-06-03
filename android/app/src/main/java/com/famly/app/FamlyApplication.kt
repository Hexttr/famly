package com.famly.app

import android.app.Application
import com.famly.app.billing.RuStoreBillingManager
import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.remote.FamlyApiClient
import com.famly.app.data.repository.FamlyRepository
import com.famly.app.data.sync.SyncRepository
import com.famly.app.data.work.RecurringWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FamlyApplication : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: FamlyRepository
        private set

    lateinit var syncRepository: SyncRepository
        private set

    lateinit var billingManager: RuStoreBillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        val db = FamlyDatabase.get(this)
        val preferences = UserPreferences(this)
        syncRepository = SyncRepository(FamlyApiClient(), db, preferences)
        repository = FamlyRepository(db, preferences, syncRepository)
        billingManager = RuStoreBillingManager(onPremiumActivated = { repository.activatePremium() })
        RecurringWorkScheduler.schedule(this)
        appScope.launch {
            repository.ensureSeeded()
            repository.processDueRecurring()
        }
    }
}
