package com.homiesgym.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(context: Context) {
    private val _premium = MutableStateFlow(false)
    val premium: StateFlow<Boolean> = _premium
    private val client = BillingClient.newBuilder(context).setListener { _, purchases -> purchases?.forEach { processPurchase(it) } }.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()

    fun connect() { if (!client.isReady) client.startConnection(object : BillingClient.BillingClientStateListener { override fun onBillingSetupFinished(r: BillingClient.BillingResult) { if (r.responseCode == BillingClient.BillingResponseCode.OK) { query(); restorePurchases() } }; override fun onBillingServiceDisconnected() {} }) }
    private fun restorePurchases() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()) { _, purchases -> purchases.forEach { processPurchase(it) } }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _premium.value = true
            if (!purchase.isAcknowledged) {
                client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) {}
            }
        }
    }

    private fun query() { val product = QueryProductDetailsParams.Product.newBuilder().setProductId("homies_premium_monthly").setProductType(ProductType.SUBS).build(); client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()) { _, result -> result.productDetailsList.firstOrNull()?.let { /* Play Console product config supplies the real offer. */ } } }
    fun launch(activity: Activity, productId: String = "homies_premium_monthly") {
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(ProductType.SUBS).build())).build()) { _, result ->
            val details = result.productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return@queryProductDetailsAsync
            val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offer.offerToken).build()
            client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        }
    }
}
