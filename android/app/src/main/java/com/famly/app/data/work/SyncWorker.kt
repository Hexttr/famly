package com.famly.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
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
    private const val PERIODIC_NAME = "famly_sync"
    private const val ONE_SHOT_TAG = "famly_sync_now"

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(ONE_SHOT_TAG)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
