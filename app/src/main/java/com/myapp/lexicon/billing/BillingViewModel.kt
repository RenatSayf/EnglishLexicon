@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER") @file:JvmName("BillingViewModelKt")

package com.myapp.lexicon.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.models.PurchaseToken
import com.myapp.lexicon.models.UserPurchase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


private const val PRODUCT_NO_ADS = "no_ads"
private const val PRODUCT_CLOUD_STORAGE = "cloud_storage"

@HiltViewModel
class BillingViewModel @Inject constructor(app: Application) : AndroidViewModel(app), PurchasesUpdatedListener {

    private var _wasCancelled = MutableLiveData(false)
    var wasCancelled : LiveData<Boolean> = _wasCancelled

    private var _noAdsProduct = MutableLiveData<Result<ProductDetails>>()
    val noAdsProduct: LiveData<Result<ProductDetails>> = _noAdsProduct

    private val _noAdsToken : MutableLiveData<PurchaseToken> = MutableLiveData(null)
    val noAdsToken: LiveData<PurchaseToken> = _noAdsToken

    private var _cloudStorageProduct = MutableLiveData<Result<ProductDetails>>()
    val cloudStorageProduct: LiveData<Result<ProductDetails>> = _cloudStorageProduct

    private var _cloudStorageToken = MutableLiveData<Result<PurchaseToken>>()
    val cloudStorageToken: LiveData<Result<PurchaseToken>> = _cloudStorageToken

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
                                            if (BuildConfig.IS_PURCHASE_TEST) {
                                                _noAdsToken.postValue(PurchaseToken.NO)
                                            } else {
                                                _noAdsToken.postValue(PurchaseToken.YES)
                                            }
                                        }
                                        else {
                                            _noAdsToken.postValue(PurchaseToken.NO)
                                        }
                                    }
                                    PRODUCT_CLOUD_STORAGE -> {
                                        if (purchase.purchaseToken.isNotEmpty()) {
                                            if (BuildConfig.IS_PURCHASE_TEST) {
                                                _cloudStorageToken.postValue(Result.success(PurchaseToken.NO))
                                            } else {
                                                _cloudStorageToken.postValue(Result.success(PurchaseToken.YES))
                                            }
                                        }
                                        else {
                                            _cloudStorageToken.postValue(Result.success(PurchaseToken.NO))
                                        }
                                    }
                                }
                            }?: run {
                                _noAdsToken.postValue(PurchaseToken.NO)
                                _cloudStorageToken.postValue(Result.failure(Throwable("******** historyRecords is null ************")))
                            }
                        }
                    })
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProductDetails(billingClient: BillingClient) {

        val productList = listOf(
            buildProduct(PRODUCT_NO_ADS),
            buildProduct(PRODUCT_CLOUD_STORAGE)
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, products ->
            if (products.isNotEmpty()) {

                try {
                    products.first {
                        it.productId == PRODUCT_NO_ADS
                    }.apply {
                        _noAdsProduct.postValue(Result.success(this))
                    }
                } catch (e: NoSuchElementException) {
                    _noAdsProduct.postValue(Result.failure(e))
                }
                try {
                    products.first {
                        it.productId == PRODUCT_CLOUD_STORAGE
                    }.apply {
                        _cloudStorageProduct.postValue(Result.success(this))
                    }
                } catch (e: NoSuchElementException) {
                    _cloudStorageProduct.postValue(Result.failure(e))
                }
            }
        }
    }

    private fun buildProduct(id: String): QueryProductDetailsParams.Product {
        return QueryProductDetailsParams.Product.newBuilder().apply {
            setProductId(id)
            setProductType(BillingClient.ProductType.INAPP)
        }.build()
    }

    fun purchaseProduct(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
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
                    val json = purchase.originalJson
                    val userPurchase = Gson().fromJson(json, UserPurchase::class.java)
                    when(userPurchase.productId) {
                        PRODUCT_NO_ADS -> {
                            if (outToken.isNotEmpty()) {
                                _noAdsToken.postValue(PurchaseToken.YES)
                            }
                            else {
                                _noAdsToken.postValue(PurchaseToken.NO)
                            }
                        }
                        PRODUCT_CLOUD_STORAGE -> {
                            if (outToken.isNotEmpty()) {
                                _cloudStorageToken.postValue(Result.success(PurchaseToken.YES))
                            }
                            else {
                                _cloudStorageToken.postValue(Result.success(PurchaseToken.NO))
                            }
                        }
                    }
                }
                else {
                    _noAdsToken.postValue(PurchaseToken.NO)
                    _cloudStorageToken.postValue(Result.failure(Throwable("Purchase has been canceled")))
                    _wasCancelled.postValue(true)
                }
                return@consumeAsync
            }
            else {
                _noAdsToken.postValue(PurchaseToken.NO)
                _cloudStorageToken.postValue(Result.failure(Throwable("Billing result: code ${billingResult.responseCode}")))
                _wasCancelled.postValue(true)
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