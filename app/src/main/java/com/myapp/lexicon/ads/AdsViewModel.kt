package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import kotlinx.serialization.json.Json


var REQUEST_ID: String? = null

class AdsViewModel(
    app: Application
): AndroidViewModel(app) {

    sealed class AdState {
        data object Init: AdState()
        data class Dismissed(val bonus: Double): AdState()
    }

    private var _interstitialAdState = MutableLiveData<AdState>(AdState.Init)
    val interstitialAdState: LiveData<AdState> = _interstitialAdState

    fun setInterstitialAdState(state: AdState) {
        _interstitialAdState.value = state
    }

    private var _adReward = MutableLiveData(0)
    val adReward: LiveData<Int> = _adReward

    fun setAdReward(value: Int) {
        _adReward.value = value
    }


}

fun String.toAdData(
    onSuccess: (AdData) -> Unit,
    onFailed: () -> Unit
) {
    try {
        val adData = Json.decodeFromString<AdData>(this)
        onSuccess.invoke(adData)
    }
    catch (e: Exception) {
        if (BuildConfig.DEBUG) e.printStackTrace()
        onFailed.invoke()
    }
}

