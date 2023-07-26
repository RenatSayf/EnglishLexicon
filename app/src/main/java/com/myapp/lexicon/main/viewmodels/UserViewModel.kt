@file:Suppress("UnnecessaryVariable", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
import android.icu.util.Currency
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.interfaces.FlowCallback
import com.myapp.lexicon.models.User
import com.myapp.lexicon.settings.getExchangeRateFromPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {
    companion object {

        private const val COLLECTION_PATH = "users"
        val USER_PERCENTAGE: Double = Firebase.remoteConfig.getDouble("USER_PERCENTAGE")
        val REVENUE_RATIO: Double = Firebase.remoteConfig.getDouble("REVENUE_RATIO")
    }

    sealed class State {
        object Start: State()
        object Complete: State()
        data class UserAdded(val user: User): State()
        data class ReceivedUserData(val user: User): State()
        data class PersonalDataUpdated(val user: User): State()
        data class RevenueUpdated(val bonus: Double, val user: User): State()
        data class PaymentRequestSent(val user: User): State()
        data class Error(val message: String): State()
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private var _stateFlow = MutableStateFlow<State>(State.Start)
    val stateFlow: StateFlow<State> = _stateFlow

    private val db: FirebaseFirestore = Firebase.firestore

    private var _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    fun setUser(user: User) {
        _user.value = user
    }

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

    private fun addUser(user: User) {
        _state.value = State.Start
        val map = user.toHashMap()
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .set(map)
            .addOnSuccessListener {
                _user.value = user
                _state.value = State.UserAdded(user)
            }
            .addOnFailureListener { ex ->
                ex.printStackTrace()
                _state.value = State.Error(ex.message?: "Unknown error")
            }
            .addOnCompleteListener {
                _state.value = State.Complete
            }
    }

    fun addUserIfNotExists(user: User) {
        _state.value = State.Start
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data
                if (data != null) {
                    _user.value = User(document.id).apply {
                        this.currency = data[User.KEY_CURRENCY].toString()
                        val reward = data[User.KEY_USER_REWARD].toString().ifEmpty {
                            0.0
                        }.toString().toDouble()
                        this.userReward = reward
                    }
                }
                else {
                    addUser(user)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                _user.value = null
            }
            .addOnCompleteListener {
                _state.value = State.Complete
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun getUserFromCloud(userId: String) {
        _state.value = State.Start
        db.collection(COLLECTION_PATH)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data as? Map<String, String?>
                if (data != null) {
                    val user = User(document.id).mapToUser(data)
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
                _state.value = State.Complete
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateUserRevenue(revenuePerAd: Double, user: User) {

        _state.value = State.Start
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.data != null) {
                    val remoteUserData = snapshot.data as Map<String, String>
                    user.reallyRevenue = calculateReallyRevenue(revenuePerAd, remoteUserData)
                    user.userReward = calculateUserReward(revenuePerAd, remoteUserData)
                    user.totalRevenue = calculateTotalRevenue(revenuePerAd, remoteUserData)
                    app.getExchangeRateFromPref(
                        onInit = {},
                        onSuccess = { date, symbol, rate ->
                            val defaultReward = BigDecimal(user.userReward * rate).setScale(2, RoundingMode.DOWN).toDouble()
                            user.defaultCurrencyReward = defaultReward
                        }
                    )
                    if (user.reallyRevenue > -1 && user.userReward > -1) {

                        val revenueMap = mapOf(
                            User.KEY_REALLY_REVENUE to user.reallyRevenue.toString(),
                            User.KEY_USER_REWARD to user.userReward.toString(),
                            User.KEY_TOTAL_REVENUE to user.totalRevenue.toString(),
                            User.KEY_DEFAULT_CURRENCY_REWARD to user.defaultCurrencyReward.toString()
                        )
                        db.collection(COLLECTION_PATH)
                            .document(user.id)
                            .update(revenueMap)
                            .addOnSuccessListener {
                                _user.value = user
                                _state.value = State.RevenueUpdated(revenuePerAd, user)
                                _stateFlow.value = State.RevenueUpdated(revenuePerAd, user)
                            }
                            .addOnFailureListener { ex ->
                                if (BuildConfig.DEBUG) {
                                    ex.printStackTrace()
                                }
                                _state.value = State.Error(ex.message?: "Unknown error")
                            }
                            .addOnCompleteListener {
                                _state.value = State.Complete
                            }
                    } else {
                        if (BuildConfig.DEBUG) {
                            val message =
                                "******************** A negative revenue value cannot be sent: ${user.reallyRevenue}, ${user.userReward} ************"
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
            }
            .addOnCompleteListener {
                _state.value = State.Complete
            }
    }

    fun updatePersonalData(user: User) {
        _state.value = State.Start
        val userMap = mapOf(
            User.KEY_PHONE to user.phone,
            User.KEY_FIRST_NAME to user.firstName,
            User.KEY_LAST_NAME to user.lastName,
            User.KEY_BANK_CARD to user.bankCard,
            User.KEY_CURRENCY to Currency.getInstance(Locale.getDefault()).currencyCode,
            User.KEY_PAYMENT_REQUIRED to user.paymentRequired.toString()
        )
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .update(userMap)
            .addOnSuccessListener {
                _user.value = user
                _state.value = State.PersonalDataUpdated(user)
            }
            .addOnFailureListener { ex ->
                if (BuildConfig.DEBUG) {
                    ex.printStackTrace()
                }
                _state.value = State.Error(ex.message?: "Unknown error")
            }
            .addOnCompleteListener {
                _state.value = State.Complete
            }
    }
    fun calculateReallyRevenue(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentRevenue = try {
            remoteUserData[User.KEY_REALLY_REVENUE]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentRevenue < 0) {
            currentRevenue
        } else {
            val newRevenue = currentRevenue + (revenuePerAd * REVENUE_RATIO)
            newRevenue
        }
    }

    fun calculateUserReward(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentReward = try {
            remoteUserData[User.KEY_USER_REWARD]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentReward < 0) {
            currentReward
        } else {
            val newReward = currentReward + ((revenuePerAd * REVENUE_RATIO) * USER_PERCENTAGE)
            newReward
        }
    }

    private fun calculateTotalRevenue(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentRevenue = try {
            remoteUserData[User.KEY_TOTAL_REVENUE]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentRevenue < 0) {
            currentRevenue
        } else {
            val newRevenue = currentRevenue + revenuePerAd
            newRevenue
        }
    }



}