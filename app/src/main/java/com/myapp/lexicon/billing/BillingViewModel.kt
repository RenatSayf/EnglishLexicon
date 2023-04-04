@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER",
    "MoveVariableDeclarationIntoWhen"
) @file:JvmName("BillingViewModelKt")

package com.myapp.lexicon.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.models.PurchaseToken
import com.myapp.lexicon.models.UserPurchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject




@HiltViewModel
class BillingViewModel @Inject constructor(app: Application) : AndroidViewModel(app), PurchasesUpdatedListener {

    private var productIdNoAds: String
    private var productIdCloudStorage: String

    private var _wasCancelled = MutableLiveData<Boolean?>(null)
    var wasCancelled : LiveData<Boolean?> = _wasCancelled

    private var _noAdsProduct = MutableLiveData<Result<ProductDetails>>()
    val noAdsProduct: LiveData<Result<ProductDetails>> = _noAdsProduct

    private val _noAdsToken : MutableLiveData<Result<PurchaseToken>> = MutableLiveData()
    val noAdsToken: LiveData<Result<PurchaseToken>> = _noAdsToken

    private var _cloudStorageProduct = MutableLiveData<Result<ProductDetails>>()
    val cloudStorageProduct: LiveData<Result<ProductDetails>> = _cloudStorageProduct

    private var _cloudStorageToken = MutableLiveData<Result<PurchaseToken>>()
    val cloudStorageToken: LiveData<Result<PurchaseToken>> = _cloudStorageToken

    val billingClient = BillingClient.newBuilder(app)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {

        productIdNoAds  = app.getString(R.string.id_no_ads)
        productIdCloudStorage  = app.getString(R.string.id_cloud_storage)

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {

                    queryProductDetails(billingClient)
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProductDetails(billingClient: BillingClient) {

        val productList = listOf(
            buildProduct(productIdNoAds),
            buildProduct(productIdCloudStorage)
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, products ->
            if (products.isNotEmpty()) {

                try {
                    products.first {
                        it.productId == productIdNoAds
                    }.apply {
                        _noAdsProduct.postValue(Result.success(this))
                    }
                } catch (e: NoSuchElementException) {
                    _noAdsProduct.postValue(Result.failure(e))
                }
                try {
                    products.first {
                        it.productId == productIdCloudStorage
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

        val json = purchase.originalJson
        val userPurchase = Gson().fromJson(json, UserPurchase::class.java)

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged)
                {
                    confirmPurchase(billingClient, purchase, onSuccess = {
                        when(userPurchase.productId) {
                            productIdNoAds -> {
                                _noAdsToken.postValue(Result.success(PurchaseToken.YES))
                            }
                            productIdCloudStorage -> {
                                _cloudStorageToken.postValue(Result.success(PurchaseToken.YES))
                            }
                        }
                    }, onCanceled = {
                        when(userPurchase.productId) {
                            productIdNoAds -> {
                                _noAdsToken.postValue(Result.failure(Throwable("Purchase has been canceled by user")))
                            }
                            productIdCloudStorage -> {
                                _cloudStorageToken.postValue(Result.failure(Throwable("Purchase has been canceled by user")))
                            }
                        }
                    }, onError = {
//                        when(userPurchase.productId) {
//                            productIdNoAds -> {
//                                _noAdsToken.postValue(Result.failure(Throwable(it)))
//                            }
//                            productIdCloudStorage -> {
//                                _cloudStorageToken.postValue(Result.failure(Throwable(it)))
//                            }
//                        }
                    })
                }
                else {
                    when(userPurchase.productId) {
                        productIdNoAds -> {
                            _noAdsToken.postValue(Result.failure(Throwable("Purchase has been canceled by user")))
                        }
                        productIdCloudStorage -> {
                            _cloudStorageToken.postValue(Result.failure(Throwable("Purchase has been canceled by user")))
                        }
                    }
                }
                return@consumeAsync
            }
            else {
//                _noAdsToken.postValue(Result.failure(Throwable("Error. Billing result: code ${billingResult.responseCode}")))
//                _cloudStorageToken.postValue(Result.failure(Throwable("Error. Billing result: code ${billingResult.responseCode}")))
//                _wasCancelled.postValue(true)
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

    private fun confirmPurchase(
        client: BillingClient,
        purchase: Purchase,
        onSuccess: (Purchase) -> Unit,
        onCanceled: (Purchase) -> Unit,
        onError: (String) -> Unit
    ) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)

        viewModelScope.launch(Dispatchers.IO) {
            val ackPurchaseResult = withContext(Dispatchers.Main) {
                client.acknowledgePurchase(acknowledgePurchaseParams.build())
            }
            val code = ackPurchaseResult.responseCode
            when (code) {
                BillingClient.BillingResponseCode.OK -> {
                    onSuccess.invoke(purchase)
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    onCanceled.invoke(purchase)
                }
                else -> {
                    onError.invoke(ackPurchaseResult.debugMessage)
                }
            }
        }

    }
}