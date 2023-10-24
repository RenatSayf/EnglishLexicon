@file:Suppress("UnnecessaryVariable", "ObjectLiteralToLambda")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.helpers.toStringTime
import com.myapp.lexicon.interfaces.FlowCallback
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
class UserViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    companion object {

        const val COLLECTION_PATH = "users"
        val USER_PERCENTAGE: Double by lazy {
            Firebase.remoteConfig.getDouble("USER_PERCENTAGE")
        }
    }

    sealed class LoadingState {
        object Start: LoadingState()
        object Complete: LoadingState()
    }

    private var _loadingState = MutableStateFlow<LoadingState>(LoadingState.Complete)
    val loadingState: StateFlow<LoadingState> = _loadingState
    fun setLoadingState(state: LoadingState) {
        _loadingState.value = state
    }

    sealed class State {
        object Init: State()
        data class UserAdded(val user: User): State()
        data class ReceivedUserData(val user: User): State()
        data class PersonalDataUpdated(val user: User): State()
        data class RevenueUpdated(val bonus: Double, val user: User): State()
        object PaymentRequestSent: State()
        data class Error(val message: String): State()
    }

    private var _state = MutableLiveData<State>(State.Init)
    val state: LiveData<State> = _state

    private var _stateFlow = MutableStateFlow<State>(State.Init)
    val stateFlow: StateFlow<State> = _stateFlow

    fun setState(state: State) {
        _state.value = state
        _stateFlow.value = state
    }

    private var _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    fun collect(callBack: FlowCallback) {
        viewModelScope.launch {
            _stateFlow.onStart {
                callBack.onStart()
            }.onCompletion {
                callBack.onCompletion(it)
            }.collect {
                callBack.onResult(_state.value)
            }
        }
    }

    fun getUserFromCloud(userId: String) {
        _loadingState.value = LoadingState.Start

        val parseQuery = ParseQuery.getQuery<ParseObject>("_User")
        parseQuery.getInBackground(userId, object : GetCallback<ParseObject> {
            override fun done(obj: ParseObject?, e: ParseException?) {
                if (obj is ParseObject) {
                    val user = obj.mapToUser(userId)
                    _user.value = user
                    _state.value = State.ReceivedUserData(user)
                    _stateFlow.value = State.ReceivedUserData(user)
                }
                else if (e is ParseException) {
                    e.printStackTrace()
                    _state.value = State.Error(e.message?: "Unknown error")
                }
                _loadingState.value = LoadingState.Complete
            }
        })
    }

    fun updateUserDataIntoCloud(userMap: Map<String, Any?>): LiveData<Result<String>> {
        _loadingState.value = LoadingState.Start
        val result = MutableLiveData<Result<String>>()

        val currentUser = ParseUser.getCurrentUser()
        userMap.forEach { entry ->
            currentUser.put(entry.key, entry.value?: "")
        }
        currentUser.saveInBackground(object : SaveCallback {
            override fun done(e: ParseException?) {
                if (e is ParseException) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace()
                    }
                    result.value = Result.failure(e)
                }
                else {
                    result.value = Result.success(currentUser.objectId)
                }
                _loadingState.value = LoadingState.Complete
            }
        })
        return result
    }

    fun updateUserRevenueIntoCloud(adData: AdData) {
        _loadingState.value = LoadingState.Start
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
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace()
                            }
                            _state.value = State.Error(e.message?: "Unknown error")
                            _stateFlow.value = State.Error(e.message?: "Unknown error")
                        }
                        else {
                            val user = currentUser.mapToUser(currentUser.objectId)
                            _user.value = user
                            _state.value = State.RevenueUpdated(adData.revenue, user)
                            _stateFlow.value = State.RevenueUpdated(adData.revenue, user)
                        }
                        _loadingState.value = LoadingState.Complete
                    }
                })
            }
            else {
                if (BuildConfig.DEBUG) {
                    val message =
                        "******************** A zero revenue value cannot be sent: ${adData.revenue} ************"
                    Throwable(message).printStackTrace()
                }
                _loadingState.value = LoadingState.Complete
            }
        }
        else _loadingState.value = LoadingState.Complete
    }

    fun updatePayoutDataIntoCloud(
        threshold: Int,
        reward: Double,
        userMap: Map<String, Any?> = mapOf(),
        onStart: () -> Unit = {},
        onSuccess: (Int, Double) -> Unit,
        onNotEnough: () -> Unit = {},
        onComplete: (Exception?) -> Unit = {}
    ) {
        onStart.invoke()
        if (reward > threshold) {
            val payout = reward.toInt()
            val remainder = reward - payout
            val currentUser = ParseUser.getCurrentUser()
            if (currentUser is ParseUser) {
                currentUser.apply {
                    put(User.KEY_PAYMENT_DATE, System.currentTimeMillis().toStringTime())
                    put(User.KEY_USER_REWARD, remainder)
                    increment(User.KEY_RESERVED_PAYMENT, payout)
                }
                userMap.forEach { entry ->
                    currentUser.put(entry.key, entry.value?: "")
                }
                currentUser.saveInBackground(object : SaveCallback {
                    override fun done(e: ParseException?) {
                        if (e is ParseException) {
                            if (BuildConfig.DEBUG) e.printStackTrace()
                            onComplete.invoke(e)
                        }
                        else {
                            onSuccess.invoke(payout, remainder)
                            onComplete.invoke(null)
                        }
                    }
                })
            }
            else {
                onComplete.invoke(Exception("************ Current user is NULL ***********"))
            }
        }
        else {
            onNotEnough.invoke()
            onComplete.invoke(null)
        }
    }

    fun<T> calcUserReward(
        revenuePerAd: Double,
        userPercentage: Double,
        remoteUserData: Map<String, Comparable<T>?>
    ): Double {
        val currentReward = remoteUserData[User.KEY_USER_REWARD]
        return if (currentReward != null && (currentReward is Number)) {
            val newReward = currentReward.toDouble() + (revenuePerAd * userPercentage)
            newReward
        }
        else {
            revenuePerAd * userPercentage
        }
    }

    private fun ParseObject.mapToUser(userId: String): User {
        return User(userId).apply {
            var value = this@mapToUser[User.KEY_REVENUE_USD]
            this.revenueUSD = if (value is Number) value.toDouble() else this.revenueUSD

            value = this@mapToUser[User.KEY_TOTAL_REVENUE]
            this.totalRevenue = if (value is Number) value.toDouble() else this.totalRevenue

            value = this@mapToUser[User.KEY_USER_REWARD]
            this.userReward = if (value is Number) value.toDouble() else this.userReward

            value = this@mapToUser[User.KEY_RESERVED_PAYMENT]
            this.reservedPayment = if (value is Number) value.toDouble() else this.reservedPayment

            value = this@mapToUser[User.KEY_CURRENCY]
            this.currency = if (value is String) value else this.currency

            value = this@mapToUser[User.KEY_EMAIL]
            this.email = if (value is String) value else this.email

            value = this@mapToUser[User.KEY_PHONE]
            this.phone = if (value is String) value else this.phone

            value = this@mapToUser[User.KEY_BANK_CARD]
            this.bankCard = if (value is String) value else this.bankCard

            value = this@mapToUser[User.KEY_BANK_NAME]
            this.bankName = if (value is String) value else this.bankName

            value = this@mapToUser[User.KEY_FIRST_NAME]
            this.firstName = if (value is String) value else this.firstName

            value = this@mapToUser[User.KEY_LAST_NAME]
            this.lastName = if (value is String) value else this.lastName

            value = this@mapToUser[User.KEY_MESSAGE]
            this.message = if (value is String) value else this.message
        }
    }


}