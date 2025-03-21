@file:Suppress("UnnecessaryVariable", "ObjectLiteralToLambda", "PropertyName")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.AdName
import com.myapp.lexicon.common.mapToUser
import com.myapp.lexicon.helpers.LOCALE_RU
import com.myapp.lexicon.helpers.toStringTime
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.userPercentFromPref
import com.parse.GetCallback
import com.parse.LogInCallback
import com.parse.LogOutCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SaveCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Currency
import javax.inject.Inject




open class UserViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    sealed class LoadingState {
        data object Start: LoadingState()
        data object Complete: LoadingState()
    }

    protected var _loadingState = MutableStateFlow<LoadingState>(LoadingState.Complete)
    open val loadingState: StateFlow<LoadingState> = _loadingState
    open fun setLoadingState(state: LoadingState) {
        _loadingState.value = state
    }

    sealed class State {
        data object Init: State()
        data class ReceivedUserData(val user: User): State()
        data object PersonalDataUpdated: State()
        data class RevenueUpdated(val bonus: Double, val user: User): State()
        data class PaymentRequestSent(val user: User, val payout: Int, val remainder: Double): State()
        data class Error(val message: String): State()
    }

    protected var _state = MutableLiveData<State>(State.Init)
    open val state: LiveData<State> = _state

    open fun setState(state: State) {
        _state.value = state
    }

    protected var _user = MutableLiveData<User?>(null)
    open val user: LiveData<User?> = _user

    protected fun signInWithCurrentUser(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        ParseUser.logOutInBackground(object : LogOutCallback {
            override fun done(e: ParseException?) {
                app.getAuthDataFromPref(
                    onSuccess = { email, password ->
                        ParseUser.logInInBackground(
                            email,
                            password,
                            object : LogInCallback {
                                override fun done(user: ParseUser?, e: ParseException?) {
                                    if (user is ParseUser) {
                                        onSuccess.invoke()
                                    }
                                    else {
                                        ParseUser.logOut()
                                        onFailure.invoke(e?.message?: "Unknown error")
                                    }
                                }
                            }
                        )
                    },
                    onNotRegistered = {
                        onFailure.invoke("*********** User not registered ***************")
                    },
                    onFailure = {
                        onFailure.invoke(app.getString(R.string.text_unknown_error_message))
                    }
                )
            }
        })
    }

    open fun getUserFromCloud(): LiveData<Result<User>> {
        _loadingState.value = LoadingState.Start

        val result = MutableLiveData<Result<User>>()
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            val parseQuery = ParseQuery.getQuery<ParseObject>("_User")
            parseQuery.getInBackground(currentUser.objectId, object : GetCallback<ParseObject> {
                override fun done(obj: ParseObject?, e: ParseException?) {
                    if (obj is ParseObject) {
                        val user = obj.mapToUser()
                        _user.value = user
                        _state.value = State.ReceivedUserData(user)
                        result.value = Result.success(user)
                    }
                    else if (e is ParseException) {
                        if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                            signInWithCurrentUser(
                                onSuccess = {
                                    getUserFromCloud()
                                },
                                onFailure = {message ->
                                    Exception(message).printStackTrace()
                                    _state.value = State.Error(message)
                                    result.value = Result.failure(Exception(message))
                                }
                            )
                        }
                        else {
                            e.printStackTrace()
                            _state.value = State.Error(e.message?: "Unknown error")
                            result.value = Result.failure(e)
                        }
                    }
                    _loadingState.value = LoadingState.Complete
                }
            })
        } else {
            result.value = Result.failure(Exception("********** ${this::class.simpleName} - current user is NULL **************"))
            _loadingState.value = LoadingState.Complete
        }
        return result
    }

    open fun updateUserDataIntoCloud(userMap: Map<String, Any?>): LiveData<Result<String>> {
        _loadingState.value = LoadingState.Start
        val result = MutableLiveData<Result<String>>()

        val currentUser = ParseUser.getCurrentUser()
        val email = userMap[User.KEY_EMAIL].toString()
        if (email.isNotEmpty()) {
            currentUser.username = email
            currentUser.email = email
        }
        userMap.forEach { entry ->
            if (AdName.entries.firstOrNull { it.name == entry.key } != null) {
                currentUser.increment(entry.key, entry.value as Number)
            }
            else {
                currentUser.put(entry.key, entry.value?: "")
            }
        }
        currentUser.saveInBackground(object : SaveCallback {
            override fun done(e: ParseException?) {
                if (e is ParseException) {
                    if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                        signInWithCurrentUser(
                            onSuccess = {
                                updateUserDataIntoCloud(userMap)
                            },
                            onFailure = {message ->
                                if (BuildConfig.DEBUG) {
                                    Exception(message).printStackTrace()
                                }
                                _state.value = State.Error(message)
                                result.value = Result.failure(Exception(message))
                            }
                        )
                    }
                    else {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace()
                        }
                        result.value = Result.failure(e)
                        _state.value = State.Error(e.message?: "Unknown error")
                    }
                }
                else {
                    result.value = Result.success(currentUser.objectId)
                    _state.value = State.PersonalDataUpdated
                }
                _loadingState.value = LoadingState.Complete
            }
        })
        return result
    }

    open fun updateUserRevenueIntoCloud(adData: AdData) {
        _loadingState.value = LoadingState.Start
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser is ParseUser) {
            if (adData.revenueUSD > 0.0) {
                currentUser.apply {
                    increment(User.KEY_REVENUE_USD, adData.revenueUSD)
                    increment(User.KEY_TOTAL_REVENUE, adData.revenue)
                    increment(User.KEY_USER_REWARD, adData.revenue * app.userPercentFromPref)
                    put(User.KEY_CURRENCY, adData.currency.toString())
                    val currencySymbol = Currency.getInstance(adData.currency).symbol
                    put(User.KEY_CURRENCY_SYMBOL, currencySymbol)
                    val currencyRate = (adData.revenue / adData.revenueUSD).to2DigitsScale()
                    put(User.KEY_CURRENCY_RATE, currencyRate)
                    put(User.KEY_APP_VERSION, "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    put(User.KEY_REWARD_UPDATE_AT, System.currentTimeMillis().toStringTime(LOCALE_RU))
                }
                currentUser.saveInBackground(object : SaveCallback {
                    override fun done(e: ParseException?) {
                        if (e is ParseException) {
                            if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                                signInWithCurrentUser(
                                    onSuccess = {},
                                    onFailure = {message ->
                                        if (BuildConfig.DEBUG) {
                                            Exception(message).printStackTrace()
                                        }
                                        _state.value = State.Error(message)
                                    }
                                )
                            }
                            else {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace()
                                }
                                _state.value = State.Error(e.message?: "Unknown error")
                            }
                        }
                        else {
                            val user = currentUser.mapToUser()
                            _user.value = user
                            _state.value = State.RevenueUpdated(adData.revenue, user)
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


}