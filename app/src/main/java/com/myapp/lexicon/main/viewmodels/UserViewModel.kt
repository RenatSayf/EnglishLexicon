@file:Suppress("UnnecessaryVariable")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
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
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {
    companion object {

        const val COLLECTION_PATH = "users"
        val USER_PERCENTAGE: Double = Firebase.remoteConfig.getDouble("USER_PERCENTAGE")
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

    private fun addUser(user: User) {
        _loadingState.value = LoadingState.Start
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
                _loadingState.value = LoadingState.Complete
            }
    }

    fun addUserIfNotExists(user: User) {
        _loadingState.value = LoadingState.Start
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
                _loadingState.value = LoadingState.Complete
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun getUserFromCloud(userId: String) {
        _loadingState.value = LoadingState.Start
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
                _loadingState.value = LoadingState.Complete
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateUserRevenue(adData: AdData, user: User) {

        _loadingState.value = LoadingState.Start
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.data != null) {
                    val remoteUserData = snapshot.data as Map<String, String>
                    user.userReward = calcUserReward(adData.revenue, remoteUserData)
                    user.totalRevenue = calcTotalRevenue(adData.revenue, remoteUserData, User.KEY_TOTAL_REVENUE)
                    user.revenueUSD = calcTotalRevenue(adData.revenueUSD, remoteUserData, User.KEY_REVENUE_USD)
                    user.currency = adData.currency
                    user.currencyRate = (adData.revenue / adData.revenueUSD).to2DigitsScale()

                    if (user.userReward > -1) {
                        val revenueMap = mapOf(
                            User.KEY_REVENUE_USD to user.revenueUSD.toString(),
                            User.KEY_USER_REWARD to user.userReward.toString(),
                            User.KEY_TOTAL_REVENUE to user.totalRevenue.toString(),
                            User.KEY_RESERVED_PAYMENT to user.reservedPayment.toString(),
                            User.KEY_CURRENCY to user.currency,
                            User.KEY_CURRENCY_SYMBOL to user.currencySymbol,
                            User.KEY_CURRENCY_RATE to user.currencyRate.toString(),
                            User.KEY_LAST_UPDATE_TIME to System.currentTimeMillis().toStringTime()
                        )
                        db.collection(COLLECTION_PATH)
                            .document(user.id)
                            .update(revenueMap)
                            .addOnSuccessListener {
                                _user.value = user
                                _state.value = State.RevenueUpdated(adData.revenue, user)
                                _stateFlow.value = State.RevenueUpdated(adData.revenue, user)
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
                                "******************** A negative revenue value cannot be sent: ${user.userReward} ************"
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

    fun updatePersonalData(user: User) {
        _loadingState.value = LoadingState.Start
        val userMap = user.toHashMap()
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .update(userMap)
            .addOnSuccessListener {
                _user.value = user
                _state.value = State.PersonalDataUpdated(user)
                _stateFlow.value = State.PersonalDataUpdated(user)
            }
            .addOnFailureListener { ex ->
                if (BuildConfig.DEBUG) {
                    ex.printStackTrace()
                }
                _state.value = State.Error(ex.message ?: "Unknown error")
                _stateFlow.value = State.Error(ex.message ?: "Unknown error")
            }
            .addOnCompleteListener {
                _loadingState.value = LoadingState.Complete
            }
    }

    fun calcUserReward(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentReward = try {
            remoteUserData[User.KEY_USER_REWARD]?.ifEmpty {
                0.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentReward < 0) {
            currentReward
        } else {
            val newReward = currentReward + (revenuePerAd * USER_PERCENTAGE)
            newReward
        }
    }

    private fun calcTotalRevenue(
        revenuePerAd: Double,
        remoteUserData: Map<String, String?>,
        mapKey: String
    ): Double {
        val currentRevenue = try {
            remoteUserData[mapKey]?.ifEmpty {
                0.0
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