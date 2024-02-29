package com.myapp.lexicon.video.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.AppRoomDbModule
import com.myapp.lexicon.di.DataRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.repository.IDataRepository
import com.myapp.lexicon.repository.network.INetRepository
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
        onComplete: () -> Unit = {},
        onFailure: (e: Exception) -> Unit = {}
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
                    onFailure.invoke(exception as Exception)
                }
            } catch (e: Exception) {
                onFailure.invoke(e)
            } finally {
                onComplete.invoke()
            }
        }
    }


}