@file:Suppress("UnnecessaryVariable")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.helpers.toStringTime
import com.myapp.lexicon.interfaces.FlowCallback
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.to2DigitsScale
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
    private val app: Application
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

    sealed class State {
        object Init: State()
        data class UserAdded(val user: User): State()
        data class ReceivedUserData(val user: User): State()
        data class PersonalDataUpdated(val user: User): State()
        data class RevenueUpdated(val bonus: Double, val user: User): State()
        data class PaymentRequestSent(val user: User): State()
        data class Error(val message: String): State()
    }

    private var _state = MutableLiveData<State>(State.Init)
    val state: LiveData<State> = _state

    private var _stateFlow = MutableStateFlow<State>(State.Init)
    val stateFlow: StateFlow<State> = _stateFlow

    private val db: FirebaseFirestore = Firebase.firestore

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
        db.collection(COLLECTION_PATH)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data as? Map<String, Any?>
                if (data != null) {
                    val user = data.mapToUser(userId)
                    _user.value = user
                    _state.value = State.ReceivedUserData(user)
                    _stateFlow.value = State.ReceivedUserData(user)
                }
                else {
                    _state.value = State.Error(app.getString(R.string.text_user_not_found))
                }
            }
            .addOnFailureListener { ex ->
                ex.printStackTrace()
                _state.value = State.Error(ex.message?: "Unknown error")
            }
            .addOnCompleteListener {
                _loadingState.value = LoadingState.Complete
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateUserRevenue(adData: AdData, userId: String) {

        _loadingState.value = LoadingState.Start
        db.collection(COLLECTION_PATH)
            .document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.data != null) {
                    if (adData.revenueUSD > 0) {
                        val remoteUserData = snapshot.data as Map<String, Comparable<Any>>
                        val userReward = calcUserReward(adData.revenue, USER_PERCENTAGE, remoteUserData)
                        val totalRevenue = calcTotalRevenue(adData.revenue, remoteUserData, User.KEY_TOTAL_REVENUE)
                        val revenueUSD = calcTotalRevenue(adData.revenueUSD, remoteUserData, User.KEY_REVENUE_USD)
                        val currency = adData.currency
                        val currencySymbol = Currency.getInstance(currency).symbol
                        val currencyRate = (adData.revenue / adData.revenueUSD).to2DigitsScale()

                        val revenueMap = mapOf(
                            User.KEY_REVENUE_USD to revenueUSD,
                            User.KEY_USER_REWARD to userReward,
                            User.KEY_TOTAL_REVENUE to totalRevenue,
                            User.KEY_CURRENCY to currency,
                            User.KEY_CURRENCY_SYMBOL to currencySymbol,
                            User.KEY_CURRENCY_RATE to currencyRate,
                            User.KEY_LAST_UPDATE_TIME to System.currentTimeMillis().toStringTime()
                        )

                        _loadingState.value = LoadingState.Start
                        db.collection(COLLECTION_PATH)
                            .document(userId)
                            .update(revenueMap)
                            .addOnSuccessListener {

                                db.collection(COLLECTION_PATH)
                                    .document(userId)
                                    .get()
                                    .addOnCompleteListener { snapshot ->
                                        if (snapshot.isSuccessful) {
                                            val data = snapshot.result.data
                                            data?.let {
                                                val user = it.mapToUser(userId)
                                                _user.value = user
                                                _state.value = State.RevenueUpdated(adData.revenue, user)
                                                _stateFlow.value = State.RevenueUpdated(adData.revenue, user)
                                            }?: run {
                                                val exception = snapshot.exception
                                                if (BuildConfig.DEBUG) {
                                                    exception?.printStackTrace()
                                                }
                                                _state.value = State.Error(exception?.message?: "Unknown error")
                                                _stateFlow.value = State.Error(exception?.message?: "Unknown error")
                                            }
                                        }
                                        else {
                                            val exception = snapshot.exception
                                            if (BuildConfig.DEBUG) {
                                                exception?.printStackTrace()
                                            }
                                            _state.value = State.Error(exception?.message?: "Unknown error")
                                            _stateFlow.value = State.Error(exception?.message?: "Unknown error")
                                        }
                                        _loadingState.value = LoadingState.Complete
                                    }
                            }
                            .addOnFailureListener { ex ->
                                if (BuildConfig.DEBUG) {
                                    ex.printStackTrace()
                                }
                                _state.value = State.Error(ex.message?: "Unknown error")
                                _stateFlow.value = State.Error(ex.message?: "Unknown error")
                            }
                            .addOnCompleteListener {
                                _loadingState.value = LoadingState.Complete
                            }
                    } else {
                        if (BuildConfig.DEBUG) {
                            val message =
                                "******************** A zero revenue value cannot be sent: ${adData.revenue} ************"
                            Throwable(message).printStackTrace()
                        }
                    }
                }
            }
            .addOnFailureListener { ex ->
                if (BuildConfig.DEBUG) {
                    ex.printStackTrace()
                }
                _state.value = State.Error(ex.message?: "Unknown error")
                _stateFlow.value = State.Error(ex.message?: "Unknown error")
            }
            .addOnCompleteListener {
                _loadingState.value = LoadingState.Complete
            }
    }

    fun addUserToStorage(userId: String, userMap: Map<String, Any?>, isNew: Boolean = false): LiveData<Result<Unit>> {
        _loadingState.value = LoadingState.Start

        val result = MutableLiveData<Result<Unit>>()
        db.collection(COLLECTION_PATH)
            .document(userId)
            .run {
                if (isNew) {
                    set(userMap)
                }
                else {
                    update(userMap)
                }
            }
            .addOnSuccessListener {
                result.value = Result.success(Unit)
            }
            .addOnFailureListener { ex ->
                if (BuildConfig.DEBUG) {
                    ex.printStackTrace()
                }
                result.value = Result.failure(ex as FirebaseFirestoreException)
            }
            .addOnCompleteListener {
                _loadingState.value = LoadingState.Complete
            }
        return result
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

    private fun<T> calcTotalRevenue(
        revenuePerAd: Double,
        remoteUserData: Map<String, Comparable<T>?>,
        mapKey: String
    ): Double {
        val currentRevenue = remoteUserData[mapKey]
        return if (currentRevenue != null && currentRevenue is Number) {
            val newRevenue = currentRevenue.toDouble() + revenuePerAd
            newRevenue
        } else {
            revenuePerAd
        }
    }

    private fun Map<String, Any?>.mapToUser(userId: String): User {
        return User(userId).apply {
            var value = this@mapToUser[User.KEY_REVENUE_USD]
            this.revenueUSD = if (value is Number) value.toDouble() else 0.0

            value = this@mapToUser[User.KEY_TOTAL_REVENUE]
            this.totalRevenue = if (value is Number) value.toDouble() else 0.0

            value = this@mapToUser[User.KEY_USER_REWARD]
            this.userReward = if (value is Number) value.toDouble() else 0.0

            value = this@mapToUser[User.KEY_CURRENCY]
            this.currency = if (value is String) value else ""

            value = this@mapToUser[User.KEY_EMAIL]
            this.email = if (value is String) value else ""

            value = this@mapToUser[User.KEY_PHONE]
            this.phone = if (value is String) value else ""

            value = this@mapToUser[User.KEY_BANK_CARD]
            this.bankCard = if (value is String) value else ""

            value = this@mapToUser[User.KEY_BANK_NAME]
            this.bankName = if (value is String) value else ""

            value = this@mapToUser[User.KEY_FIRST_NAME]
            this.firstName = if (value is String) value else ""

            value = this@mapToUser[User.KEY_LAST_NAME]
            this.lastName = if (value is String) value else ""

            value = this@mapToUser[User.KEY_MESSAGE]
            this.message = if (value is String) value else ""
        }
    }


}