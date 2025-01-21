@file:Suppress("RedundantSamConstructor", "MoveVariableDeclarationIntoWhen", "PropertyName",
    "ObjectLiteralToLambda"
)

package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.auth.models.SBPBanks
import com.myapp.lexicon.common.EXPLAIN_MESSAGE
import com.myapp.lexicon.common.IS_BANK_CARD_REQUIRED
import com.myapp.lexicon.common.PAYMENT_CODE
import com.myapp.lexicon.common.PAYMENT_DAYS
import com.myapp.lexicon.common.PAYMENT_THRESHOLD
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.Tokens
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SaveCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

open class AccountViewModel(
    netModule: INetRepositoryModule
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AccountViewModel::class.java)
            return AccountViewModel(netModule) as T
        }
    }

    sealed class State {
        data object ReadOnly: State()
        data object Editing: State()
    }

    open val paymentThreshold: Double = PAYMENT_THRESHOLD
    open val paymentCode: String = PAYMENT_CODE
    open val paymentDays: Int = PAYMENT_DAYS
    open val explainMessage: String = EXPLAIN_MESSAGE
    open val isBankCardRequired: Boolean = IS_BANK_CARD_REQUIRED

    private var thread: Thread? = null
    private var payoutThread: Thread? = null
    private val repository = netModule.provideNetRepository()

    private var _state = MutableLiveData<State>().apply {
        value = State.ReadOnly
    }
    open val state: LiveData<State> = _state

    open fun setState(state: State) {
        _state.value = state
    }

    private var _screenState = MutableLiveData<AccountScreenState>(AccountScreenState.Init)
    open val screenState: LiveData<AccountScreenState> = _screenState
    fun saveScreenState(state: AccountScreenState) {
        _screenState.value = state
    }

    protected var _bankList = MutableLiveData<Result<List<String>>>()
    open val bankList: LiveData<Result<List<String>>> = _bankList

    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    var newTokens = MutableStateFlow<Result<Tokens>>(Result.failure(Throwable()))
        private set

    var authorizationRequired = MutableStateFlow<Result<Boolean>>(Result.failure(Throwable()))
        private set

    open fun fetchBankListFromNet() {

        thread = Thread(Runnable {
            val url = "https://sbp.nspk.ru/rest/v1/banks/list?limit=500"
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val inputStream = BufferedInputStream(urlConnection.inputStream)
                val responseText = inputStream.bufferedReader().readText()
                val code = urlConnection.responseCode
                when(code) {
                    200 -> {
                        try {
                            val sbpBanks = jsonDecoder.decodeFromString<SBPBanks>(responseText)
                            val bankList = sbpBanks.banks.map {
                                it.title
                            }
                            _bankList.postValue(Result.success(bankList))
                        } catch (e: Exception) {
                            e.printStackTraceIfDebug()
                            getBankListFromCloud()
                        }
                    }
                    else -> {
                        _bankList.postValue(Result.failure(Exception("********* ${AccountViewModel::class.simpleName}.fetchBankList() - Http response code - $code **************")))
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                _bankList.postValue(Result.failure(e))
            }
        })
        thread?.start()
    }

    open fun getBankListFromCloud() {
        val query = ParseQuery.getQuery<ParseObject>("Banks")
        query.getFirstInBackground(object : GetCallback<ParseObject> {
            override fun done(obj: ParseObject?, e: ParseException?) {
                when {
                    obj is ParseObject -> {
                        val strJson = obj["Names"].toString()
                        val bankList = Json.decodeFromString<List<String>>(strJson)
                        _bankList.value = Result.success(bankList)
                    }
                    e is ParseException -> {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace()
                        }
                        _bankList.value = Result.failure(e)
                    }
                }
            }
        })
    }

    fun demandPayment(
        threshold: Int,
        reward: Int,
        userMap: Map<String, Any?> = mapOf(),
        onStart: () -> Unit = {},
        onSuccess: () -> Unit,
        onNotEnough: () -> Unit = {},
        onInvalidToken: (String) -> Unit,
        onComplete: (Exception?) -> Unit = {}
    ) {
        onStart.invoke()
        if (reward > threshold) {
            val currentUser = ParseUser.getCurrentUser()
            if (currentUser is ParseUser) {
                userMap.forEach { entry ->
                    currentUser.put(entry.key, entry.value?: "")
                }
                currentUser.saveInBackground(object : SaveCallback {
                    override fun done(e: ParseException?) {
                        if (e is ParseException) {
                            if (e.code == ParseException.INVALID_SESSION_TOKEN) {
                                onInvalidToken.invoke(currentUser.sessionToken)
                                onComplete.invoke(null)
                            }
                            else {
                                if (BuildConfig.DEBUG) e.printStackTrace()
                                onComplete.invoke(e)
                            }
                        }
                        else {
                            onSuccess.invoke()
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

    fun sendPaymentInfoToTGChannel(
        message: String,
        onStart: () -> Unit = {},
        onSuccess: () -> Unit = {},
        onComplete: (Exception?) -> Unit = {}
    ) {
        onStart.invoke()
        val botToken = BuildConfig.BOT_TOKEN
        val chatId = BuildConfig.CHAT_ID

        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$message"

        try {
            payoutThread = Thread(Runnable {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val code = urlConnection.responseCode
                when(code) {
                    200 -> {
                        onSuccess.invoke()
                    }
                    else -> {
                        val exception = Exception(urlConnection.responseMessage)
                        onComplete.invoke(exception)
                    }
                }
            })
        } catch (e: Exception) {
            onComplete.invoke(e)
        } finally {
            onComplete.invoke(null)
        }
        payoutThread?.start()
    }

    fun signOut(
        token: String,
        onStart: () -> Unit = {},
        onSuccess: (Tokens) -> Unit = {},
        onComplete: (Exception?) -> Unit = {},
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        onStart.invoke()
        viewModelScope.launch(dispatcher) {
            repository.signOut(token).collect(collector = { result ->
                result.onSuccess { tokens: Tokens ->
                    onSuccess.invoke(tokens)
                }
                result.onFailure { exception: Throwable ->
                    onComplete.invoke(exception as Exception)
                }
            })
        }
    }

    override fun onCleared() {
        thread?.interrupt()
        payoutThread?.interrupt()
        super.onCleared()
    }

    init {
        netModule.apply {
            setTokensUpdateListener(object : INetRepositoryModule.Listener {
                override fun onUpdateTokens(tokens: Tokens) {
                    setRefreshToken(tokens.refreshToken)
                    newTokens.value = Result.success(tokens)
                }

                override fun onAuthorizationRequired() {
                    authorizationRequired.value = Result.success(true)
                }
            })
        }
        this.fetchBankListFromNet()
    }
}