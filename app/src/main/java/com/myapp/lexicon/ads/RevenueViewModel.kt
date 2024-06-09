@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.mapToRevenue
import com.myapp.lexicon.common.mapToUser
import com.myapp.lexicon.helpers.printStackTraceIfDebug
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
import java.util.Currency
import javax.inject.Inject



class RevenueViewModel @Inject constructor(
    app: Application
): UserViewModel(app) {

    private var _userRevenueLD = MutableLiveData<AppResult>(AppResult.Init)
    val userRevenueLD: LiveData<AppResult> = _userRevenueLD

    override fun updateUserRevenueIntoCloud(adData: AdData) {

        val currentUser = ParseUser.getCurrentUser()
        if (currentUser is ParseUser) {
            if (adData.revenueUSD > 0.0 && REQUEST_ID != adData.requestId) {
                REQUEST_ID = adData.requestId
                currentUser.apply {
                    increment(User.KEY_REVENUE_USD, adData.revenueUSD)
                    increment(User.KEY_TOTAL_REVENUE, adData.revenue)

                    val userReward = adData.revenue * USER_PERCENTAGE
                    increment(User.KEY_USER_REWARD, userReward)
                    increment(User.KEY_USER_DAILY_REWARD, userReward)

                    val revenueFromUser = adData.revenue - userReward
                    increment(User.KEY_DAILY_REVENUE_FROM_USER, revenueFromUser)

                    put(User.KEY_CURRENCY, adData.currency.toString())
                    val currencySymbol = Currency.getInstance(adData.currency).symbol
                    put(User.KEY_CURRENCY_SYMBOL, currencySymbol)
                    val currencyRate = (adData.revenue / adData.revenueUSD).to2DigitsScale()
                    put(User.KEY_CURRENCY_RATE, currencyRate)
                }
                currentUser.saveInBackground(object : SaveCallback {
                    override fun done(e: ParseException?) {
                        if (e is ParseException) {
                            if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                                signInWithCurrentUser(
                                    onSuccess = {},
                                    onFailure = {message ->
                                        Exception(message).printStackTraceIfDebug()
                                        _userRevenueLD.value = AppResult.Error(Exception(e.message))
                                    }
                                )
                            }
                            else {
                                _userRevenueLD.value = AppResult.Error(Exception(e.message))
                            }
                        }
                        else {
                            val query = ParseQuery<ParseObject>("_User")
                            query.getInBackground(currentUser.objectId, object : GetCallback<ParseObject> {
                                override fun done(obj: ParseObject?, e: ParseException?) {
                                    when {
                                        obj is ParseObject -> {
                                            val revenue = obj.mapToRevenue()
                                            _userRevenueLD.value = AppResult.Success(revenue)
                                            val user = obj.mapToUser()
                                            _state.value = State.RevenueUpdated(adData.revenue * USER_PERCENTAGE, user)
                                        }
                                        e is ParseException -> {
                                            _userRevenueLD.value = AppResult.Error(Exception(e.message))
                                        }
                                    }
                                }
                            })
                        }
                    }
                })
            }
            else {
                val exception = Exception("******************** A zero revenue value cannot be sent: ${adData.revenue} ************")
                _userRevenueLD.value = AppResult.Error(Exception(exception))
            }
        }
    }

    fun resetUserDailyReward(user: User) {
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser is ParseUser) {
            currentUser.apply {
                put(User.KEY_USER_DAILY_REWARD, 0.0)
                put(User.KEY_DAILY_REVENUE_FROM_USER, 0.0)
                put(User.KEY_YESTERDAY_USER_REWARD, user.userDailyReward)
                put(User.KEY_YESTERDAY_REVENUE_FROM_USER, user.dailyRevenueFromUser)
            }
            saveUserIntoCloud(currentUser)
        }
    }

    private fun saveUserIntoCloud(user: ParseUser){
        user.saveInBackground(object : SaveCallback {
            override fun done(e: ParseException?) {
                if (e is ParseException) {
                    if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                        signInWithCurrentUser(
                            onSuccess = {
                                saveUserIntoCloud(user)
                            },
                            onFailure = {message ->
                                Exception(message).printStackTraceIfDebug()
                            }
                        )
                    }
                    else {
                        e.printStackTraceIfDebug()
                    }
                }
            }
        })
    }


}