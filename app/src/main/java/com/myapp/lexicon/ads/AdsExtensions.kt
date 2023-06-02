package com.myapp.lexicon.ads

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient


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






























