package com.myapp.lexicon.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdsViewModel @Inject constructor(private val app: Application) : AndroidViewModel(app)
{
    fun getMainBanner() : AdView
    {
        return AdView(app).apply {
            adSize = AdSize.BANNER
            adUnitId = if (BuildConfig.DEBUG)
            {
                app.getString(R.string.tests_banner)
            } else
            {
                app.getString(R.string.main_bottom_banner)
            }
        }
    }

    fun getEditorBanner() : AdView
    {
        return AdView(app).apply {
            adSize = AdSize.BANNER
            adUnitId = if (BuildConfig.DEBUG)
            {
                app.getString(R.string.tests_banner)
            } else
            {
                app.getString(R.string.word_editor_bottom_banner)
            }
        }
    }

    fun getAddWordBanner() : AdView
    {
        return AdView(app).apply {
            adSize = AdSize.BANNER
            adUnitId = if (BuildConfig.DEBUG)
            {
                app.getString(R.string.tests_banner)
            } else
            {
                app.getString(R.string.add_word_bottom_banner)
            }
        }
    }

    private var interstitialAd1: InterstitialAd? = null
    private var interstitialAd2: InterstitialAd? = null

    private var _isAdLoaded1 = MutableLiveData<Boolean?>(null)
    var isAdLoaded1: LiveData<Boolean?> = _isAdLoaded1

    private var _isAdFailed1 = MutableLiveData(-1)
    var isAdFailed1: LiveData<Int> = _isAdFailed1

    private var _isAdClosed1 = MutableLiveData(false)
    var isAdClosed1: LiveData<Boolean> = _isAdClosed1

    private var _isAdLoaded2 = MutableLiveData<Boolean?>(null)
    var isAdLoaded2: LiveData<Boolean?> = _isAdLoaded2

    private var _isAdFailed2 = MutableLiveData(-1)
    var isAdFailed2: LiveData<Int> = _isAdFailed2

    private var _isAdClosed2 = MutableLiveData(false)
    var isAdClosed2: LiveData<Boolean> = _isAdClosed2
    fun showAd1(activity: Activity)
    {
        _isAdLoaded1.value?.let {
            if (it)
            {
                interstitialAd1?.show(activity)
            }
            else _isAdLoaded1.value = false
        }
    }

    fun showAd2(activity: Activity)
    {
        _isAdLoaded2.value?.let {
            if (it)
            {
                interstitialAd2?.show(activity)
            }
            else _isAdLoaded1.value = false
        }
    }

    fun loadAd1()
    {
        val adRequest = AdRequest.Builder().build()
        val adId = if (BuildConfig.DEBUG)
        {
            R.string.test_interstitial_ad
        }
        else R.string.main_interstitial_ad
        InterstitialAd.load(app, app.getString(adId), adRequest, object : InterstitialAdLoadCallback()
        {
            override fun onAdFailedToLoad(p0: LoadAdError)
            {
                _isAdFailed1.value = p0.code
                _isAdLoaded1.value = false
                println("************************ interstitialAd1 onAdFailedToLoad() error : ${p0.message} code: ${p0.code} **************************")
            }

            override fun onAdLoaded(ad: InterstitialAd)
            {
                interstitialAd1 = ad
                _isAdLoaded1.value = true
                interstitialAd1!!.fullScreenContentCallback = object : FullScreenContentCallback()
                {
                    override fun onAdDismissedFullScreenContent()
                    {
                        _isAdClosed1.value = true
                        loadAd1()
                    }
                }
            }
        })
    }

    fun loadAd2()
    {
        val adRequest = AdRequest.Builder().build()
        val adId = if (BuildConfig.DEBUG)
        {
            R.string.test_interstitial_ad
        }
        else R.string.add_word_interstitial_ad
        InterstitialAd.load(app, app.getString(adId), adRequest, object : InterstitialAdLoadCallback()
        {
            override fun onAdFailedToLoad(p0: LoadAdError)
            {
                _isAdFailed2.value = p0.code
                _isAdLoaded2.value = false
                println("************************ interstitialAd1 onAdFailedToLoad() error : ${p0.message} code: ${p0.code} **************************")
            }

            override fun onAdLoaded(ad: InterstitialAd)
            {
                interstitialAd2 = ad
                _isAdLoaded2.value = true
                interstitialAd2!!.fullScreenContentCallback = object : FullScreenContentCallback()
                {
                    override fun onAdDismissedFullScreenContent()
                    {
                        _isAdClosed2.value = true
                        loadAd2()
                    }
                }
            }
        })
    }


}