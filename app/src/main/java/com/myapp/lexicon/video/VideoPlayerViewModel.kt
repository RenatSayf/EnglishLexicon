@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.video

import android.accounts.Account
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthUtil
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.captions.CaptionList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class VideoPlayerViewModel(
    private val app: Application,
    private val videoId: String?,
    private val repository: INetRepository
) : AndroidViewModel(app) {

    class Factory(
        private val videoId: String?,
        private val repository: INetRepository = NetRepositoryModule.provideNetRepository()
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoPlayerViewModel::class.java)
            return VideoPlayerViewModel(app = App.INSTANCE, videoId = this.videoId, repository = this.repository) as T
        }
    }

    private var _authToken = MutableLiveData<Result<String>>()
    val authToken: LiveData<Result<String>> = _authToken

    fun getAuthToken(account: Account) {
        val tokenScope = "oauth2:https://www.googleapis.com/auth/youtube.force-ssl"

        Thread(object : Runnable {
            override fun run() {
                try {
                    val token = GoogleAuthUtil.getToken(app.applicationContext, account, tokenScope)
                    _authToken.postValue(Result.success(token))
                } catch (e: Exception) {
                    _authToken.postValue(Result.failure(e))
                }
            }
        }).start()
    }

    private var _captionListResult = MutableSharedFlow<Result<CaptionList>>()
    val captionListResult: MutableSharedFlow<Result<CaptionList>> = _captionListResult

    fun getCaptionsList(authToken: String) {

        viewModelScope.launch {
            videoId?.let {
                val result = repository.fetchCaptionsList(it, authToken).await()
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
            }?: run {
                Exception("********* getCaptionsList(): videoId is NULL ************").printStackTraceIfDebug()
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