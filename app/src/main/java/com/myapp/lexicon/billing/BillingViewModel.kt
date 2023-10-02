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
import com.myapp.lexicon.R
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

    private var currentPurchase: ProductDetails? = null

    private var _wasCancelled = MutableLiveData<Result<ProductDetails>>()
    var wasCancelled : LiveData<Result<ProductDetails>> = _wasCancelled

    private var _noAdsProduct = MutableLiveData<Result<ProductDetails>>()
    val noAdsProduct: LiveData<Result<ProductDetails>> = _noAdsProduct

    private val _noAdsToken : MutableLiveData<Result<String>> = MutableLiveData()
    val noAdsToken: LiveData<Result<String>> = _noAdsToken

    private var _cloudStorageProduct = MutableLiveData<Result<ProductDetails>>()
    val cloudStorageProduct: LiveData<Result<ProductDetails>> = _cloudStorageProduct

    private var _cloudStorageToken = MutableLiveData<Result<String>>()
    val cloudStorageToken: LiveData<Result<String>> = _cloudStorageToken

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
        currentPurchase = null
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
        currentPurchase = productDetails
    }

    private fun handlePurchase(billingClient: BillingClient, purchase: Purchase)
    {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val json = purchase.originalJson
        val userPurchase = UserPurchase.fromJson(json)

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        confirmPurchase(billingClient, purchase, onSuccess = {
                            when(userPurchase.productId) {
                                productIdNoAds -> {
                                    _noAdsToken.postValue(Result.success(userPurchase.purchaseToken))
                                }
                                productIdCloudStorage -> {
                                    _cloudStorageToken.postValue(Result.success(userPurchase.purchaseToken))
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
                            when(userPurchase.productId) {
                                productIdNoAds -> {
                                    _noAdsToken.postValue(Result.failure(Throwable("************ ${billingResult.debugMessage} *****************")))
                                }
                                productIdCloudStorage -> {
                                    _cloudStorageToken.postValue(Result.failure(Throwable("************ ${billingResult.debugMessage} *****************")))
                                }
                            }
                        })
                    } else {
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
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach {
                handlePurchase(billingClient, it)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            currentPurchase?.let {
                _wasCancelled.value = Result.success(it)
            }
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
                in (1..4) -> {
                    onError.invoke(ackPurchaseResult.debugMessage)
                }
                else -> {
                    onSuccess.invoke(purchase)
                }
            }
        }

    }
}