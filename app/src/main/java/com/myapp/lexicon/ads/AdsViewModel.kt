package com.myapp.lexicon.ads

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.KEY_BILLING
import com.myapp.lexicon.billing.KEY_PURCHASE_TOKEN
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdsViewModel @Inject constructor(private val app: Application) : AndroidViewModel(app)
{
    private val tokenNoAds = app.getSharedPreferences(KEY_BILLING, Context.MODE_PRIVATE).getString(KEY_PURCHASE_TOKEN, "")!!

    private var _mainBanner = MutableLiveData<AdView>(null).apply {
//        if (tokenNoAds.isEmpty())
//        {
//            value = AdView(app).apply {
//                adSize = AdSize.BANNER
//                adUnitId = if (BuildConfig.DEBUG)
//                {
//                    app.getString(R.string.tests_banner)
//                }
//                else
//                {
//                    app.getString(R.string.main_bottom_banner)
//                }
//            }
//        }
    }
    var mainBanner: LiveData<AdView> = _mainBanner

    private var _token = MutableLiveData("").apply {
        value = app.getSharedPreferences(KEY_BILLING, Context.MODE_PRIVATE).getString(KEY_PURCHASE_TOKEN, "")!!
    }
    var token: LiveData<String> = _token
}