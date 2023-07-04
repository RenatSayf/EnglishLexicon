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
import com.myapp.lexicon.models.currency.Currency
import com.myapp.lexicon.models.currency.RubUsd
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

    private var thread: Thread? = null
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private var _currency = MutableLiveData<AppResult>(AppResult.Init)
    val currency: LiveData<AppResult> = _currency

    fun getExchangeRateFromApi(
        locale: Locale = Locale.getDefault(),
        onSuccess: (Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        when(locale.toLanguageTag().lowercase()) {
            "ru-ru" -> {
                fetchActualRate(
                    RubUsd.name,
                    onSuccess = { rate ->
                        onSuccess.invoke(rate)
                    },
                    onFailure = { exception ->
                        onFailure.invoke(exception)
                    }
                )
            }
            else -> {
                onSuccess.invoke(1.0)
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
                        if (result is RubUsd) {
                            onSuccess.invoke(result.rate)
                        }
                        else {
                            onFailure.invoke(Exception())
                        }
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
                RubUsd.name -> {
                    RubUsd(System.currentTimeMillis().toStringDate(), currency, value)
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
        val currencyName = if (locale.toLanguageTag().lowercase() == "ru-ru") {
            "RUB"
        } else null

        currencyName?.let { currency ->
            db.collection(COLLECTION_PATH)
                .document(currency)
                .get()
                .addOnSuccessListener { snapshot ->
                    val data = snapshot.data
                    data?.let {
                        val date = it["date"].toString()
                        val name = it["currency"].toString()
                        val rate = it["rate"].toString().toDouble()
                        _currency.value = AppResult.Success(RubUsd(date, name, rate))
                    }?: run {
                        _currency.value = AppResult.Error(Exception("No data"))
                    }
                }
                .addOnFailureListener { ex ->
                    ex.printStackTrace()
                    _currency.value = AppResult.Error(ex)
                }
        }?: run {
            _currency.value = AppResult.Error(Exception("No data"))
        }
    }

    fun saveExchangeRateToCloud(currency: Currency) {
        db.collection(COLLECTION_PATH)
            .document(currency.name)
            .set(currency.toMap())
            .addOnSuccessListener {

            }
            .addOnFailureListener { ex ->
                ex.printStackTrace()
            }
    }

    override fun onCleared() {

        thread?.interrupt()
        super.onCleared()
    }
}