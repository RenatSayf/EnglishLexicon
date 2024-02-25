package com.myapp.lexicon.video.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.coroutines.launch
import java.util.Locale

class VideoListViewModel(
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

    private var _searchResult = MutableLiveData<Result<VideoSearchResult>>()
    val searchResult: LiveData<Result<VideoSearchResult>> = _searchResult

    fun fetchSearchResult(searchString: String) {
        viewModelScope.launch {
            try {
                val result = repository.getSearchResult(searchString).await()
                _searchResult.postValue(if (result != null) Result.success(result) else Result.failure(Exception("Search result is NULL")))
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                _searchResult.postValue(Result.failure(e))
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
        fetchSearchResult("")
    }

}