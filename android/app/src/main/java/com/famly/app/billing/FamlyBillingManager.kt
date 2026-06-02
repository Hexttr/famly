package com.famly.app.billing

/**
 * RuStore Pay SDK integration stub.
 *
 * When ready for production:
 * 1. Add dependency: ru.rustore.sdk:billingclient (see RuStore docs)
 * 2. Configure console_app_id in AndroidManifest
 * 3. Implement purchase flow below
 * 4. Forward subscription events to backend POST /webhooks/rustore
 */
object FamlyProducts {
    const val PREMIUM_MONTHLY = "famly_premium_monthly"
    const val PREMIUM_YEARLY = "famly_premium_yearly"
}

interface FamlyBillingManager {
    suspend fun purchaseMonthly()
    suspend fun purchaseYearly()
    suspend fun restorePurchases(): Boolean
    suspend fun isPremiumActive(): Boolean
}

class RuStoreBillingManagerStub : FamlyBillingManager {
    override suspend fun purchaseMonthly() {
        // RuStorePayClient.instance.getPurchaseInteractor().purchase(...)
    }

    override suspend fun purchaseYearly() {
        // RuStorePayClient.instance.getPurchaseInteractor().purchase(...)
    }

    override suspend fun restorePurchases(): Boolean = false

    override suspend fun isPremiumActive(): Boolean = false
}
