@file:Suppress("PropertyName")

package com.myapp.lexicon.video.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.AppRoomDbModule
import com.myapp.lexicon.di.DataRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.repository.IDataRepository
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.models.query.HistoryQuery
import kotlinx.coroutines.launch

open class VideoListViewModel(
    private val netRepository: INetRepository,
    private val dbRepository: IDataRepository
) : ViewModel() {

    class Factory(
        private val netRepository: INetRepository = NetRepositoryModule.provideNetRepository(),
        private val dbRepository: IDataRepository = DataRepositoryModule.provideDataRepository(AppRoomDbModule.provideAppRoomDb(App.INSTANCE))
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoListViewModel::class.java)
            return VideoListViewModel(netRepository = this.netRepository, dbRepository = this.dbRepository) as T
        }
    }

    protected var isSearchResultLoading = false
    protected var _searchResult = MutableLiveData<Result<VideoSearchResult>>()
    open val searchResult: LiveData<Result<VideoSearchResult>> = _searchResult

    open fun fetchSearchResult(
        query: String,
        pageToken: String,
        subtitles: Boolean = true,
        maxResults: Int = if (BuildConfig.DEBUG) 3 else 10
    ) {
        if (!isSearchResultLoading) {
            viewModelScope.launch {
                isSearchResultLoading = true
                try {
                    val result = netRepository.getSearchResult(query, pageToken, maxResults, subtitles).await()
                    result.onSuccess { value: VideoSearchResult ->
                        _searchResult.postValue(Result.success(value.apply {
                            this.query = query
                        }))
                    }
                    result.onFailure { exception ->
                        _searchResult.postValue(Result.failure(exception))
                    }

                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                    _searchResult.postValue(Result.failure(e))
                } finally {
                    isSearchResultLoading = false
                }
            }
        }
    }

    fun getLastVideoFromHistory(
        onResult: (item: HistoryQuery?) -> Unit,
        onStart: () -> Unit = {},
        onComplete: (t: Throwable?) -> Unit = {}
    ) {
        onStart.invoke()
        viewModelScope.launch {
            try {
                val result = dbRepository.getLatestVideoFromHistory().await()
                result.onSuccess { value: HistoryQuery? ->
                    onResult.invoke(value)
                }
                result.onFailure { exception ->
                    onComplete.invoke(exception)
                    return@launch
                }
            } finally {
                onComplete.invoke(null)
            }
        }
    }



}