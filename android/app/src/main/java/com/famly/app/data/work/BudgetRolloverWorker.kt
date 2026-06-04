package com.famly.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.famly.app.FamlyApplication
import com.famly.app.domain.budget.BudgetRolloverProcessor
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class BudgetRolloverWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as FamlyApplication
        app.repository.processBudgetRollover()
        return Result.success()
    }
}

object BudgetRolloverWorkScheduler {
    private const val WORK_NAME = "budget_rollover"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<BudgetRolloverWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
