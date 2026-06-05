package com.famly.app.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.famly.app.FamlyApplication
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as FamlyApplication
        val settings = app.repository.settings.first()
        if (settings.authToken.isNullOrBlank() || settings.householdId.isNullOrBlank()) {
            return Result.success()
        }
        val status = app.syncRepository.sync()
        return if (status.success) Result.success() else Result.retry()
    }
}

object SyncWorkScheduler {
    private const val PERIODIC_NAME = "famly_sync_periodic"
    private const val DEBOUNCED_NAME = "famly_sync_debounced"
    private const val FOREGROUND_MIN_INTERVAL_MS = 45_000L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /** Coalesce local edits: wait briefly, then push pending changes and pull updates. */
    fun scheduleDebounced(context: Context, delaySeconds: Long = 20) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DEBOUNCED_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun scheduleOnForeground(context: Context, lastAttemptMs: Long) {
        if (System.currentTimeMillis() - lastAttemptMs < FOREGROUND_MIN_INTERVAL_MS) return
        scheduleDebounced(context, delaySeconds = 2)
    }
}
