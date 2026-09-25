package com.homiesgym.app.billing
import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(context: Context) {
 companion object { const val MONTHLY="homies_premium_monthly"; const val YEARLY="homies_premium_yearly" }
 private val _premium=MutableStateFlow(false); val premium:StateFlow<Boolean> = _premium
 private val client=BillingClient.newBuilder(context).setListener{_,purchases->purchases?.forEach(::processPurchase)}
  .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()
 fun connect(){if(client.isReady)return;client.startConnection(object:BillingClientStateListener{
  override fun onBillingSetupFinished(r:BillingResult){if(r.responseCode==BillingClient.BillingResponseCode.OK)restorePurchases()}
  override fun onBillingServiceDisconnected()=Unit})}
 private fun restorePurchases(){client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()){_,purchases->purchases.forEach(::processPurchase)}}
 private fun processPurchase(p:Purchase){if(p.purchaseState==Purchase.PurchaseState.PURCHASED){_premium.value=true;if(!p.isAcknowledged)client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()){}}}
 fun launch(activity:Activity,productId:String=MONTHLY){client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build())).build()){r,list->if(r.responseCode!=BillingClient.BillingResponseCode.OK)return@queryProductDetailsAsync;val p=list.productDetailsList.firstOrNull()?:return@queryProductDetailsAsync;val o=p.subscriptionOfferDetails?.firstOrNull()?:return@queryProductDetailsAsync;val params=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(p).setOfferToken(o.offerToken).build();client.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())}}
}