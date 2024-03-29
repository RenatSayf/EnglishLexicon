@file:Suppress("ObjectLiteralToLambda") @file:JvmName("BillingViewModelKt")

package com.myapp.lexicon.billing

import android.content.Context
import com.android.billingclient.api.*
import com.myapp.lexicon.R
import com.myapp.lexicon.models.UserPurchase
import javax.inject.Inject


class UserPurchases @Inject constructor (
    context: Context,
    private val listener: Listener
    ) : PurchasesUpdatedListener {

    interface Listener {
        fun onExistsAdsToken(token: String)
        fun onEmptyAdsToken()
        fun onExistsCloudToken(token: String)
        fun onEmptyCloudToken()
    }

    private var productIdNoAds: String
    private var productIdCloudStorage: String

    init {

        productIdNoAds  = context.getString(R.string.id_no_ads)
        productIdCloudStorage  = context.getString(R.string.id_cloud_storage)

        val billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {

                    billingClient.queryPurchaseHistoryAsync(params, object : PurchaseHistoryResponseListener {
                        override fun onPurchaseHistoryResponse(
                            result: BillingResult,
                            historyRecords: MutableList<PurchaseHistoryRecord>?
                        ) {
                            historyRecords?.forEach {
                                val json = it.originalJson
                                val purchase = UserPurchase.fromJson(json)

                                when(purchase.productId) {
                                    productIdNoAds -> {
                                        if (purchase.purchaseToken.isNotEmpty()) {
                                            listener.onExistsAdsToken(purchase.purchaseToken)
                                        }
                                        else {
                                            listener.onEmptyAdsToken()
                                        }
                                    }
                                    productIdCloudStorage -> {
                                        if (purchase.purchaseToken.isNotEmpty()) {
                                            listener.onExistsCloudToken(purchase.purchaseToken)
                                        }
                                        else {
                                            listener.onEmptyCloudToken()
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            }
            override fun onBillingServiceDisconnected() {
                listener.onEmptyAdsToken()
                listener.onEmptyCloudToken()
            }
        })
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {}


}