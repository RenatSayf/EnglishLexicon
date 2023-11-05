@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.mapToRevenue
import com.myapp.lexicon.interfaces.FlowCallback
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.AppResult
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.to2DigitsScale
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SaveCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject


@HiltViewModel
class RevenueViewModel @Inject constructor(
    app: Application
): UserViewModel(app) {

    private var _userRevenue = MutableStateFlow<AppResult>(AppResult.Init)
    val userRevenue: StateFlow<AppResult> = _userRevenue

    fun collectRevenue(callBack: FlowCallback) {
        viewModelScope.launch {
            _userRevenue.onStart {
                callBack.onStart()
            }.onCompletion { throwable ->
                if (throwable?.message != "Job was cancelled") {
                    callBack.onCompletion(throwable)
                }
            }.collect {
                callBack.onResult(_userRevenue.value)
            }
        }
    }

    override fun updateUserRevenueIntoCloud(adData: AdData) {

        val currentUser = ParseUser.getCurrentUser()
        if (currentUser is ParseUser) {
            if (adData.revenueUSD > 0.0) {
                currentUser.apply {
                    increment(User.KEY_REVENUE_USD, adData.revenueUSD)
                    increment(User.KEY_TOTAL_REVENUE, adData.revenue)
                    increment(User.KEY_USER_REWARD, adData.revenue * USER_PERCENTAGE)
                    put(User.KEY_CURRENCY, adData.currency.toString())
                    val currencySymbol = Currency.getInstance(adData.currency).symbol
                    put(User.KEY_CURRENCY_SYMBOL, currencySymbol)
                    val currencyRate = (adData.revenue / adData.revenueUSD).to2DigitsScale()
                    put(User.KEY_CURRENCY_RATE, currencyRate)
                }
                currentUser.saveInBackground(object : SaveCallback {
                    override fun done(e: ParseException?) {
                        if (e is ParseException) {
                            _userRevenue.value = AppResult.Error(e)
                        }
                        else {
                            val query = ParseQuery<ParseObject>("_User")
                            query.getInBackground(currentUser.objectId, object : GetCallback<ParseObject> {
                                override fun done(obj: ParseObject?, e: ParseException?) {
                                    when {
                                        obj is ParseObject -> {
                                            val revenue = obj.mapToRevenue()
                                            _userRevenue.value = AppResult.Success(revenue)
                                        }
                                        e is ParseException -> {
                                            _userRevenue.value = AppResult.Error(e)
                                        }
                                    }
                                }
                            })
                        }
                    }
                })
            }
            else {
                if (BuildConfig.DEBUG) {
                    val message =
                        "******************** A zero revenue value cannot be sent: ${adData.revenue} ************"
                    Throwable(message).printStackTrace()
                }
            }
        }
    }

}