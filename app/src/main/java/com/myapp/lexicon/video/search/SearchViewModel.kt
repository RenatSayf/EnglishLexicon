package com.myapp.lexicon.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.AppRoomDbModule
import com.myapp.lexicon.di.DataRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.repository.IDataRepository
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.query.HistoryQuery
import com.myapp.lexicon.video.models.query.SearchQuery
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel(
    private val netRepository: INetRepository,
    private val dbRepository: IDataRepository
) : ViewModel() {

    class Factory(
        private val netRepository: INetRepository = NetRepositoryModule.provideNetRepository(),
        private val dbRepository: IDataRepository = DataRepositoryModule.provideDataRepository(AppRoomDbModule.provideAppRoomDb(App.INSTANCE))
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == SearchViewModel::class.java)
            return SearchViewModel(netRepository, dbRepository) as T
        }
    }

    fun fetchSuggestions(
        query: String,
        lang: String = Locale.getDefault().language,
        onStart: () -> Unit = {},
        onResult: (result: Result<List<SearchQuery>>) -> Unit = {},
        onComplete: (t: Throwable?) -> Unit = {}
    ) {
        onStart.invoke()
        viewModelScope.launch {
            try {
                val result = netRepository.fetchSuggestions(query, lang).await()
                result.onSuccess { value: List<String> ->
                    val queryList = value.map {
                        SearchQuery(it)
                    }
                    onResult.invoke(Result.success(queryList))
                }
                result.onFailure { exception ->
                    onComplete.invoke(exception)
                }
            } finally {
                onComplete.invoke(null)
            }
        }
    }

    fun getVideoHistory(
        onStart: () -> Unit = {},
        onComplete: (t: Throwable?) -> Unit = {},
        onSuccess: (list: List<HistoryQuery>) -> Unit
    ) {
        onStart.invoke()
        viewModelScope.launch {
            try {
                val result = dbRepository.getVideoHistory().await()
                result.onSuccess { list: List<HistoryQuery> ->
                    onSuccess.invoke(list)
                }
                result.onFailure { exception ->
                    onComplete.invoke(exception)
                }
            } finally {
                onComplete.invoke(null)
            }
        }
    }


}