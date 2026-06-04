package com.famly.app.billing

import com.famly.app.data.sync.SyncRepository

/**
 * RuStore Pay SDK integration with server subscription sync.
 *
 * Production checklist:
 * 1. Add dependency: ru.rustore.sdk:billingclient (see RuStore docs)
 * 2. Configure console_app_id in AndroidManifest
 * 3. Replace stub purchase calls with RuStorePayClient
 * 4. Forward subscription events to backend POST /webhooks/rustore
 */
object FamlyProducts {
    const val PREMIUM_MONTHLY = "famly_premium_monthly"
    const val PREMIUM_YEARLY = "famly_premium_yearly"
}

enum class PremiumPlan { MONTHLY, YEARLY }

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data class Failure(val message: String) : PurchaseResult()
    data object Cancelled : PurchaseResult()
}

interface FamlyBillingManager {
    suspend fun purchaseMonthly(): PurchaseResult
    suspend fun purchaseYearly(): PurchaseResult
    suspend fun restorePurchases(): Boolean
    suspend fun isPremiumActive(): Boolean
}

/**
 * Stub implementation that simulates RuStore purchase and refreshes server subscription status.
 */
class RuStoreBillingManager(
    private val onPremiumActivated: suspend () -> Unit,
    private val syncRepository: SyncRepository? = null,
) : FamlyBillingManager {

    override suspend fun purchaseMonthly(): PurchaseResult {
        // RuStorePayClient.instance.getPurchaseInteractor().purchase(FamlyProducts.PREMIUM_MONTHLY)
        return completePurchase()
    }

    override suspend fun purchaseYearly(): PurchaseResult {
        // RuStorePayClient.instance.getPurchaseInteractor().purchase(FamlyProducts.PREMIUM_YEARLY)
        return completePurchase()
    }

    override suspend fun restorePurchases(): Boolean {
        syncRepository?.refreshPremiumStatus()
        return syncRepository?.let {
            runCatching {
                it.refreshPremiumStatus()
                true
            }.getOrDefault(false)
        } ?: false
    }

    override suspend fun isPremiumActive(): Boolean {
        syncRepository?.refreshPremiumStatus()
        return false
    }

    private suspend fun completePurchase(): PurchaseResult {
        onPremiumActivated()
        syncRepository?.refreshPremiumStatus()
        return PurchaseResult.Success
    }
}

/** No-op billing for development without RuStore SDK. */
class NoOpBillingManager : FamlyBillingManager {
    override suspend fun purchaseMonthly(): PurchaseResult = PurchaseResult.Cancelled
    override suspend fun purchaseYearly(): PurchaseResult = PurchaseResult.Cancelled
    override suspend fun restorePurchases(): Boolean = false
    override suspend fun isPremiumActive(): Boolean = false
}
