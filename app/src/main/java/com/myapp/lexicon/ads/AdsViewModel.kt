package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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

}