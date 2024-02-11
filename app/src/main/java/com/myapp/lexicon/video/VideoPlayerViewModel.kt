@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.di.App

class VideoPlayerViewModel(
    private val app: Application,
    val videoId: String?
) : AndroidViewModel(app) {

    class Factory(private val videoId: String?): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoPlayerViewModel::class.java)
            return VideoPlayerViewModel(app = App.INSTANCE, videoId = this.videoId) as T
        }
    }

    private val am: AccountManager = AccountManager.get(app)

    fun getAuthToken(
        videoId: String,
        account: Account
    ) {
        val authScope = "https://www.googleapis.com/auth/youtube.force-ssl"
        val options = Bundle().apply {
            putString("key", "AIzaSyBF5uCkyXVQLVUtclUBcHDQqklEf9JAMq4")
            putString("part", "snippet")
            putString("videoId", videoId)
        }
        am.getAuthToken(
            account,
            authScope,
            options,
            true,
            object : AccountManagerCallback<Bundle> {
                override fun run(future: AccountManagerFuture<Bundle>?) {
                    val bundle = future?.result
                    val token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                    token
                }
            },
            Handler(Looper.getMainLooper(), object : Handler.Callback {
                override fun handleMessage(p0: Message): Boolean {
                    val data = p0.data
                    return false
                }
            })
        )
    }

}