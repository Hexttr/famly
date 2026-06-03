package com.famly.app.billing

/**
 * RuStore Pay SDK integration.
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
 * Stub implementation that simulates a successful RuStore purchase.
 * Calls [onPremiumActivated] when purchase succeeds.
 */
class RuStoreBillingManager(
    private val onPremiumActivated: suspend () -> Unit,
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
        // Query RuStore for active subscriptions and call onPremiumActivated if found
        return false
    }

    override suspend fun isPremiumActive(): Boolean = false

    private suspend fun completePurchase(): PurchaseResult {
        onPremiumActivated()
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
