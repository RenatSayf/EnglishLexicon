@file:Suppress("RedundantSamConstructor", "MoveVariableDeclarationIntoWhen", "PropertyName")

package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.models.User
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

open class AccountViewModel : ViewModel() {

    sealed class State {
        object ReadOnly: State()
        object Editing: State()
    }

    open val paymentThreshold: Double = Firebase.remoteConfig.getDouble("payment_threshold")
    open val paymentDays: Int = Firebase.remoteConfig.getDouble("payment_days").toInt()
    open val explainMessage: String = Firebase.remoteConfig.getString("reward_explain_message")

    private var thread: Thread? = null

    private var _state = MutableLiveData<State>().apply {
        value = State.ReadOnly
    }
    open val state: LiveData<State> = _state

    open fun setState(state: State) {
        _state.value = state
    }

    private var _screenState = MutableLiveData<AccountScreenModel>(AccountScreenModel.Init)
    open val screenState: LiveData<AccountScreenModel> = _screenState
    fun saveScreenState(state: AccountScreenModel) {
        _screenState.value = state
    }

    protected var _bankList = MutableLiveData<Result<List<String>>>()
    open val bankList: LiveData<Result<List<String>>> = _bankList

    open fun fetchBankList() {

        thread = Thread(Runnable {
            val url = "https://sbp.nspk.ru/participants/"
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val inputStream = BufferedInputStream(urlConnection.inputStream)
                val responseText = inputStream.bufferedReader().readText()
                val code = urlConnection.responseCode
                when(code) {
                    200 -> {
                        val document = Jsoup.parse(responseText)
                        val elements = document.select(".bank-name")
                        val list = elements.map {
                            it.text()
                        }
                        _bankList.postValue(Result.success(list))
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

    override fun onCleared() {
        thread?.interrupt()
        super.onCleared()
    }

    init {
        this.fetchBankList()
    }
}