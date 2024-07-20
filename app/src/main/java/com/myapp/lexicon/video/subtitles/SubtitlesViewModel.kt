@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video.subtitles

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.captions.CaptionList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File


class SubtitlesViewModel(
    private val app: Application,
    private val repository: INetRepository
): AndroidViewModel(app) {

    companion object {
        const val requestCode = 3578942
    }

    class Factory(
        private val repository: INetRepository = NetRepositoryModule.provideNetRepository()
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == SubtitlesViewModel::class.java)
            return SubtitlesViewModel(app = App.INSTANCE, repository = this.repository) as T
        }
    }

    fun createChooseAccountIntent(onCreated: (intent: Intent, requestCode: Int) -> Unit) {
        val accountIntent =
            AccountPicker.newChooseAccountIntent(
                AccountPicker.AccountChooserOptions.Builder()
                    .apply {
                        this.setAlwaysShowAccountPicker(true)
                        setAllowableAccountsTypes(
                            listOf(
                                GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE
                            )
                        )
                    }.build()
            )
        onCreated.invoke(accountIntent, requestCode) // in the fragment, call startActivityForResult(accountIntent, requestCode)
    }

    // override this method in the fragment
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == SubtitlesViewModel.requestCode) {
            val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val password = data?.getStringExtra(AccountManager.KEY_PASSWORD)
            if (accountName != null) {
                val account = Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                getAuthToken(account)
            }
        }
    }

    fun getAuthToken(account: Account) {
        val tokenScope = "oauth2:https://www.googleapis.com/auth/youtube.force-ssl"

        Thread(object : Runnable {
            override fun run() {
                try {
                    val token = GoogleAuthUtil.getToken(app.applicationContext, account, tokenScope)

                } catch (e: Exception) {
                    e.throwIfDebug()
                }
            }
        }).start()
    }

    private var _captionListResult = MutableSharedFlow<Result<CaptionList>>()
    val captionListResult: MutableSharedFlow<Result<CaptionList>> = _captionListResult

    fun getCaptionsList(authToken: String, videoId: String) {

        viewModelScope.launch {
            val result = repository.fetchCaptionsList(videoId, authToken).await()
            result.onSuccess { value: CaptionList? ->
                if (value != null) {
                    _captionListResult.emit(Result.success(value))
                }
                else {
                    _captionListResult.emit(Result.failure(Throwable("NULL")))
                }
            }
            result.onFailure { exception ->
                _captionListResult.emit(Result.failure(exception))
            }
        }
    }

    fun loadCaptions(captionsId: String, authToken: String) {
        viewModelScope.launch {
            val result = repository.downloadCaptions(captionsId, authToken).await()
            result.onSuccess { value: File ->
                value
            }
            result.onFailure { exception ->
                exception
            }
        }
    }

}