@file:Suppress("RedundantSamConstructor", "MoveVariableDeclarationIntoWhen", "PropertyName",
    "ObjectLiteralToLambda"
)

package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.auth.models.SBPBanks
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

open class AccountViewModel : ViewModel() {

    sealed class State {
        object ReadOnly: State()
        object Editing: State()
    }

    open val paymentThreshold: Double = if (!BuildConfig.DEBUG)
        Firebase.remoteConfig.getDouble("payment_threshold") else 0.1

    open val paymentCode: String = if (!BuildConfig.DEBUG)
        Firebase.remoteConfig.getString("PAYMENT_CODE") else BuildConfig.PAYMENT_CODE

    open val paymentDays: Int = Firebase.remoteConfig.getDouble("payment_days").toInt()
    open val explainMessage: String = Firebase.remoteConfig.getString("reward_explain_message")
    open val isBankCardRequired: Boolean = Firebase.remoteConfig.getBoolean("is_bank_card_required")

    private var thread: Thread? = null
    private var payoutThread: Thread? = null

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

    override fun onCleared() {
        thread?.interrupt()
        payoutThread?.interrupt()
        super.onCleared()
    }

    init {
        this.fetchBankListFromNet()
    }
}