package com.famly.app

import android.app.Application
import com.famly.app.data.local.FamlyDatabase
import com.famly.app.data.local.UserPreferences
import com.famly.app.data.repository.FamlyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FamlyApplication : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: FamlyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = FamlyDatabase.get(this)
        repository = FamlyRepository(db, UserPreferences(this))
        appScope.launch { repository.ensureSeeded() }
    }
}
