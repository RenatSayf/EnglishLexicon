package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Context
import com.appodeal.ads.Appodeal
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.appodeal.ads.utils.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R


fun Context.getAdvertisingID(
    onSuccess: (String) -> Unit,
    onUnavailable: () -> Unit,
    onFailure: (String) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val client = AdvertisingIdClient(this)
    var thread: Thread? = null
    thread = Thread {
        try {
            client.start()
            val clientInfo = client.info
            val id = clientInfo.id
            id?.let {
                onSuccess.invoke(it)
            }?: run {
                onUnavailable.invoke()
            }
        } catch (e: Exception) {
            onFailure.invoke(e.message?: "Unknown error")
        }
        finally {
            thread?.let {
                if (it.isAlive) {
                    it.interrupt()
                }
            }
            onComplete.invoke()
        }
    }
    thread.start()
}

fun Activity.adsInitialize(
    onSuccess: () -> Unit,
    onFailed: (List<ApdInitializationError>) -> Unit = {}
) {
    Appodeal.initialize(
        activity = this,
        appKey = this.getString(R.string.appodeal_app_key),
        adTypes = Appodeal.BANNER or Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO,
        callback = object : ApdInitializationCallback {
            override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                errors?.let {
                    onFailed.invoke(it)
                }?: run {
                    if (BuildConfig.DEBUG) {
                        Appodeal.setTesting(true)
                        Appodeal.setLogLevel(Log.LogLevel.verbose)
                    }
                    else {
                        Appodeal.setTesting(false)
                        Appodeal.setLogLevel(Log.LogLevel.none)
                    }
                    onSuccess.invoke()
                }
                errors?.forEach {
                    if (BuildConfig.DEBUG) {
                        it.printStackTrace()
                    }
                }
            }
        }
    )
}






























