@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.myapp.lexicon.models.UserPurchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


private const val PRODUCT_NO_ADS = "no_ads"

@HiltViewModel
class DonateViewModel @Inject constructor(app: Application) : AndroidViewModel(app), PurchasesUpdatedListener {

    private val _noAdsToken : MutableLiveData<String?> = MutableLiveData(null)
    val noAdsToken: LiveData<String?> = _noAdsToken

    private var _wasCancelled = MutableLiveData(false)
    var wasCancelled : LiveData<Boolean> = _wasCancelled

    private var _noAdsProduct = MutableLiveData<ProductDetails>()
    val noAdsProduct: LiveData<ProductDetails> = _noAdsProduct

    val billingClient = BillingClient.newBuilder(app)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {

        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {

                    queryProductDetails(billingClient)

                    billingClient.queryPurchaseHistoryAsync(params, object : PurchaseHistoryResponseListener {
                        override fun onPurchaseHistoryResponse(
                            result: BillingResult,
                            historyRecords: MutableList<PurchaseHistoryRecord>?
                        ) {
                            historyRecords?.forEach {
                                val json = it.originalJson
                                val purchase = Gson().fromJson(json, UserPurchase::class.java)

                                when(purchase.productId) {
                                    PRODUCT_NO_ADS -> {
                                        if (purchase.purchaseToken.isNotEmpty()) {
                                            _noAdsToken.postValue(purchase.purchaseToken)
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun queryProductDetails(billingClient: BillingClient) {

        val productList = listOf(
            buildProduct(PRODUCT_NO_ADS)
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, products ->
            if (products.isNotEmpty()) {

                val noAdsProduct = products.first {
                    it.productId == PRODUCT_NO_ADS
                }
                _noAdsProduct.postValue(noAdsProduct)
            }
        }
    }

    private fun buildProduct(id: String): QueryProductDetailsParams.Product {
        return QueryProductDetailsParams.Product.newBuilder().apply {
            setProductId(id)
            setProductType(BillingClient.ProductType.INAPP)
        }.build()
    }

    fun disableAds(activity: Activity)
    {
        noAdsProduct.value?.let {  details ->

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    private fun handlePurchase(billingClient: BillingClient, purchase: Purchase)
    {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())

                                when(result.responseCode) {

                                    BillingClient.BillingResponseCode.OK -> {
                                        val json = purchase.originalJson
                                        val userPurchase = Gson().fromJson(json, UserPurchase::class.java)
                                        when(userPurchase.productId) {
                                            PRODUCT_NO_ADS -> {
                                                if (purchase.purchaseToken.isNotEmpty()) {
                                                    _noAdsToken.postValue(purchase.purchaseToken)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                return@consumeAsync
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(billingClient, purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _wasCancelled.value = true
        }
    }
}