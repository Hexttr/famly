package com.famly.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.famly.app.FamlyApplication
import java.util.concurrent.TimeUnit

class RecurringTransactionWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as FamlyApplication
        app.repository.processDueRecurring()
        return Result.success()
    }
}

object RecurringWorkScheduler {
    private const val WORK_NAME = "recurring_transactions"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
