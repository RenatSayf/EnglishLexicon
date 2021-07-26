package com.myapp.lexicon.billing

import android.app.Activity
import android.app.Application
import android.content.Context
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


const val KEY_PURCHASE_TOKEN = "KEY_PURCHASE_TOKEN_575456645458"
const val KEY_BILLING = "KEY_BILLING_525454554"
private const val PRODUCT_ID = "no_ads"

@Suppress("ObjectLiteralToLambda")
@HiltViewModel
class BillingViewModel @Inject constructor(private val app: Application) : AndroidViewModel(app)
{
    private var skuDetails: SkuDetails? = null

    private val billingClient = BillingClient.newBuilder(app)
        .setListener(object : PurchasesUpdatedListener
        {
            override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?)
            {
                if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null)
                {
                    purchases.forEach {
                        handlePurchase(it)
                    }
                }
                else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED)
                {
                    _wasCancelled.value = true
                }
            }
        })
        .enablePendingPurchases()
        .build()

    private var _noAdsToken = MutableLiveData<String?>(null)
    val noAdsToken: LiveData<String?> = _noAdsToken //TODO перед релизом раскоментировать _noAdsToken

    private var _wasCancelled = MutableLiveData(false)
    var wasCancelled : LiveData<Boolean> = _wasCancelled

    private var _priceText = MutableLiveData("")
    var priceText: LiveData<String> = _priceText

    fun querySkuDetails()
    {
        var skuDetailsResult: SkuDetailsResult?
        val skuList = ArrayList<String>()
        skuList.add(PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        viewModelScope.launch {
            skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient.querySkuDetails(params.build())
            }
            val list = skuDetailsResult?.skuDetailsList
            if (!list.isNullOrEmpty())
            {
                skuDetails = list.first()
                skuDetails.let { d ->
                    val price = d?.price
                    _priceText.value = price
                }
            }
            return@launch
        }
    }

    private fun handlePurchase(purchase: Purchase)
    {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                {
                    _noAdsToken.value = purchase.purchaseToken
                    app.getSharedPreferences(KEY_BILLING, Context.MODE_PRIVATE).edit().putString(KEY_PURCHASE_TOKEN, purchase.purchaseToken).apply()
                    if (!purchase.isAcknowledged)
                    {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        viewModelScope.launch {
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                        }
                    }
                }
                return@consumeAsync
            }
        }
    }

    fun disableAds(activity: Activity)
    {
        skuDetails?.let {
            val flowParams = BillingFlowParams.newBuilder().setSkuDetails(it).build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    init
    {
        val token = app.getSharedPreferences(KEY_BILLING, Context.MODE_PRIVATE).getString(KEY_PURCHASE_TOKEN, "")
        if (!token.isNullOrEmpty())
        {
            _noAdsToken.value = token
        }
        else
        {
            billingClient.startConnection(object : BillingClientStateListener
            {
                override fun onBillingSetupFinished(result: BillingResult)
                {
                    querySkuDetails()
                    viewModelScope.launch {
                        val purchases = billingClient.queryPurchaseHistory(BillingClient.SkuType.INAPP)
                        val purchasesList = purchases.purchaseHistoryRecordList
                        purchasesList?.let { list ->
                            list.forEach {
                                val json = it.originalJson
                                val purchase = Gson().fromJson(json, UserPurchase::class.java)
                                if (purchase.productId == PRODUCT_ID && purchase.purchaseToken.isNotEmpty())
                                {
                                    app.getSharedPreferences(KEY_BILLING, Context.MODE_PRIVATE).edit().putString(KEY_PURCHASE_TOKEN, purchase.purchaseToken).apply()
                                    _noAdsToken.postValue(purchase.purchaseToken)
                                    return@launch
                                }
                            }
                            _noAdsToken.postValue("")
                        }
                    }
                }

                override fun onBillingServiceDisconnected()
                {
                    _noAdsToken.postValue("ZZZZZZZZZZZZZZZZZZZZ")
                }
            })
        }
    }
}