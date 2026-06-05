package com.famly.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper

class TestFamlyApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
