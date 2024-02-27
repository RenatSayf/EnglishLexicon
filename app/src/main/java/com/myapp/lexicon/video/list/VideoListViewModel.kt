@file:Suppress("PropertyName")

package com.myapp.lexicon.video.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.coroutines.launch
import java.util.Locale

open class VideoListViewModel(
    private val repository: INetRepository
) : ViewModel() {

    class Factory(
        private val repository: INetRepository
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoListViewModel::class.java)
            return VideoListViewModel(repository) as T
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
                    val result = repository.getSearchResult(query, pageToken, maxResults, subtitles).await()
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

    fun fetchSuggestions(query: String, lang: String = Locale.getDefault().language): LiveData<Result<List<String>>> {
        val result = MutableLiveData<Result<List<String>>>()
        viewModelScope.launch {
            result.value = repository.fetchSuggestions(query, lang).await()
        }
        return result
    }

    init {
        fetchSearchResult(query = "friends", pageToken = "")
    }

}