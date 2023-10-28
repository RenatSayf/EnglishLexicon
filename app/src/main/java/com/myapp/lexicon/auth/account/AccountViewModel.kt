@file:Suppress("RedundantSamConstructor", "MoveVariableDeclarationIntoWhen")

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
        data class OnSave(val user: User): State()
        data class OnValid(
            var phone: Boolean = true,
            var bankName: Boolean = true,
            var card: Boolean = true,
            var firstName: Boolean = true,
            var lastName: Boolean = true
        ): State()
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

    open fun fetchBankList(): LiveData<Result<List<String>>> {

        val result = MutableLiveData<Result<List<String>>>()
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
                        result.postValue(Result.success(list))
                    }
                    else -> {
                        result.postValue(Result.failure(Exception("********* ${AccountViewModel::class.simpleName}.fetchBankList() - Http response code - $code **************")))
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                result.postValue(Result.failure(e))
            }
        })
        thread?.start()

        return result
    }

    override fun onCleared() {
        thread?.interrupt()
        super.onCleared()
    }
}