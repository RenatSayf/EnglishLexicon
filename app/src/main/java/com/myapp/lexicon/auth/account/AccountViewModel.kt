@file:Suppress("RedundantSamConstructor", "MoveVariableDeclarationIntoWhen", "PropertyName",
    "ObjectLiteralToLambda"
)

package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.auth.AuthViewModel.Companion.ACCOUNT_DELETING_ERROR
import com.myapp.lexicon.auth.models.SBPBanks
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.HttpThrowable
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.repository.network.INetRepository
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext




open class AccountViewModel(
    private val netModule: INetRepositoryModule,
    private val coroutineContext: CoroutineContext = Dispatchers.Main
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

    sealed class LoadingState {
        data object Start: LoadingState()
        data object Complete: LoadingState()
    }

    protected var _loadingState = MutableLiveData<LoadingState>()
    open val loadingState: LiveData<LoadingState> = _loadingState

    open fun setLoadingState(state: LoadingState) {
        _loadingState.postValue(state)
    }

    open val paymentDays: Int = Firebase.remoteConfig.getDouble("payment_days").toInt()
    open val explainMessage: String = Firebase.remoteConfig.getString("reward_explain_message")
    open val isBankCardRequired: Boolean = try {
        Firebase.remoteConfig.getBoolean("is_bank_card_required")
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
        false
    }

    private var thread: Thread? = null
    private var payoutThread: Thread? = null

    protected open var _screenState = MutableLiveData<AccountScreenState>(AccountScreenState.Init)
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

    sealed interface AuthState {
        data class TokensUpdated(val tokens: Tokens): AuthState
        data object AuthorizationRequired: AuthState
        data object LogOut: AuthState
        data object AccountDeleting: AuthState
        data class HttpError(val throwable: HttpThrowable): AuthState
    }

    protected open var _authState = MutableLiveData<AuthState>()
    open val authState: LiveData<AuthState> = _authState

    protected open val repository: INetRepository = netModule.apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                this@apply.setRefreshToken(tokens.refreshToken)
                _authState.postValue(AuthState.TokensUpdated(tokens))
            }

            override fun onAuthorizationRequired() {
                _authState.postValue(AuthState.AuthorizationRequired)
            }
        })
    }.provideNetRepository()

    open fun setRefreshToken(token: String) {
        netModule.setRefreshToken(token)
    }

    open fun fetchBankListFromNet() {

        thread = Thread(Runnable {
            val url = "https://sbp.nspk.ru/rest/v1/banks/list?limit=500"
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("Content-Type", "application/json")
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
        accessToken: String,
        userMap: Map<String, Any>,
        onStart: () -> Unit = {},
        onSuccess: (user: UserX) -> Unit,
        onNotEnough: () -> Unit = {},
        onInvalidToken: () -> Unit,
        onComplete: (HttpThrowable?) -> Unit = {}
    ) {
        onStart.invoke()
        viewModelScope.launch(context = this.coroutineContext) {
            repository.reservedPaymentToUser(
                accessToken = accessToken,
                map = userMap
            ).collect(collector = { result ->
                result.onSuccess { user ->
                    onSuccess.invoke(user)
                }
                var throwable: HttpThrowable? = null
                result.onFailure { t ->
                    throwable = t.castToHttpThrowable()
                    when(throwable.errorCode) {
                        401 -> {
                            onInvalidToken.invoke()
                        }
                        403 -> {
                            onNotEnough.invoke()
                        }
                        else -> {
                            onComplete.invoke(throwable)
                            return@collect
                        }
                    }
                }
                onComplete.invoke(throwable)
            })
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

    fun signOut(token: String) {
        _loadingState.value = LoadingState.Start
        viewModelScope.launch(context = this.coroutineContext) {
            repository.signOut(token).collect(collector = { result ->
                result.onSuccess { tokens: Tokens ->
                    _authState.value = AuthState.LogOut
                }
                result.onFailure { t ->
                    val throwable = t.castToHttpThrowable()
                    _authState.value = AuthState.HttpError(throwable)
                }
            })
            _loadingState.value = LoadingState.Complete
        }
    }

    fun deleteUserAccount(token: String) {
        _loadingState.value = LoadingState.Start
        viewModelScope.launch(context = this.coroutineContext) {
            repository.deleteUser(token).collect(collector = { result ->
                result.onSuccess { value: Boolean ->
                    if (value) {
                        _authState.value = AuthState.AccountDeleting
                    }
                    else {
                        val throwable = Exception(ACCOUNT_DELETING_ERROR).castToHttpThrowable()
                        _authState.value = AuthState.HttpError(throwable)
                    }
                }
                result.onFailure { t: Throwable ->
                    val throwable = t.castToHttpThrowable()
                    _authState.value = AuthState.HttpError(throwable)
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
        //this.fetchBankListFromNet()
        this.getBankListFromCloud()
        this.fetchBankListFromNet()
    }
}