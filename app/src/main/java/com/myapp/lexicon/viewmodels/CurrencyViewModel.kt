@file:Suppress("MoveVariableDeclarationIntoWhen", "RedundantSamConstructor")

package com.myapp.lexicon.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.models.AppResult
import com.myapp.lexicon.models.currency.Currencies
import com.myapp.lexicon.models.currency.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject


private const val COLLECTION_PATH = "currencies"

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    sealed class State {
        object Init: State()
        data class Updated(val currency: Currency): State()
        data class Error(val exception: Exception): State()
    }

    private var thread: Thread? = null
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private var _currency = MutableLiveData<AppResult>(AppResult.Init)
    val currency: LiveData<AppResult> = _currency

    private var _state = MutableLiveData<State>(State.Init)
    val state: LiveData<State> = _state

    fun getExchangeRateFromApi(
        locale: Locale = Locale.getDefault(),
        onSuccess: (Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currency = android.icu.util.Currency.getInstance(locale)
        when(currency.currencyCode) {
            Currencies.USD.name -> {
                onSuccess.invoke(1.0)
            }
            else -> {
                fetchActualRate(
                    currency.currencyCode,
                    onSuccess = { rate ->
                        onSuccess.invoke(rate)
                    },
                    onFailure = { exception ->
                        onFailure.invoke(exception)
                    }
                )
            }
        }
    }

    private fun fetchActualRate(
        currency: String,
        onSuccess: (Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        thread = Thread(Runnable {

            val apiKey = app.getString(R.string.currency_converter_api_key)
            val url = "https://api.freecurrencyapi.com/v1/latest?apikey=$apiKey&currencies=$currency"

            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val inputStream = BufferedInputStream(urlConnection.inputStream)
                val responseText = inputStream.bufferedReader().readText()
                val code = urlConnection.responseCode
                when(code) {
                    200 -> {
                        val result = parsingCurrencyApiResult(responseText, currency)
                        if (result is Currency) {
                            onSuccess.invoke(result.rate)
                        }
                        else {
                            onFailure.invoke(Exception())
                        }
                    }
                    422 -> {
                        onSuccess.invoke(1.0)
                    }
                    else -> {
                        onFailure.invoke(Exception(responseText))
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                onFailure.invoke(e)
            }
        })
        thread?.start()
    }

    fun parsingCurrencyApiResult(json: String, currency: String): Currency? {
        return try {
            val jsonData = JSONObject(json).getJSONObject("data")
            val value = jsonData.getDouble(currency)
            when(currency) {
                Currencies.RUB.name -> {
                    Currency(System.currentTimeMillis().toStringDate(), currency, value)
                }
                else -> {
                    null
                }
            }
        }
        catch (e: JSONException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            null
        }
    }

    fun fetchExchangeRateFromCloud(locale: Locale = Locale.getDefault()) {
        val currency = android.icu.util.Currency.getInstance(locale)
        val currencyCode = currency.currencyCode

        if (currencyCode == Currencies.USD.name) {
            _currency.value = AppResult.Success(Currency(
                System.currentTimeMillis().toStringDate(),
                name = Currencies.USD.name,
                rate = 1.0
            ))
            return
        }

        db.collection(COLLECTION_PATH)
            .document(currencyCode)
            .get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.data
                data?.let {
                    val date = it["date"].toString()
                    val name = it["currency"].toString()
                    val rate = it["rate"].toString().toDouble()
                    _currency.value = AppResult.Success(Currency(date, name, rate))
                }?: run {
                    _currency.value = AppResult.Success(Currency(
                        0L.toStringDate(),
                        name = Currencies.USD.name,
                        rate = 1.0
                    ))
                }
            }
            .addOnFailureListener { ex ->
                ex.printStackTrace()
                _currency.value = AppResult.Error(ex)
            }
    }

    fun saveExchangeRateToCloud(currency: Currency) {
        db.collection(COLLECTION_PATH)
            .document(currency.name)
            .set(currency.toMap())
            .addOnSuccessListener {
                _state.value = State.Updated(currency)
            }
            .addOnFailureListener { ex ->
                ex.printStackTrace()
                _state.value = State.Error(ex)
            }
    }

    override fun onCleared() {

        thread?.interrupt()
        super.onCleared()
    }
}