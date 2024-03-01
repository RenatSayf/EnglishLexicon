@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen", "UnnecessaryVariable")

package com.myapp.lexicon.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.AppRoomDbModule
import com.myapp.lexicon.di.DataRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.repository.IDataRepository
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.list.VideoListViewModel
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.models.query.HistoryQuery
import kotlinx.coroutines.launch

class VideoPlayerViewModel(
    private val netRepository: INetRepository,
    private val dbRepository: IDataRepository
) : VideoListViewModel(netRepository) {

    class Factory(
        private val netRepository: INetRepository = NetRepositoryModule.provideNetRepository(),
        private val dbRepository: IDataRepository = DataRepositoryModule.provideDataRepository(AppRoomDbModule.provideAppRoomDb(App.INSTANCE))
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoPlayerViewModel::class.java)
            return VideoPlayerViewModel(netRepository = this.netRepository, dbRepository = this.dbRepository) as T
        }
    }

    var videoTimeMarker: Float = 0.0f

    var searchQuery: String = ""
        private set
    var pageToken: String = ""
        private set

    override val searchResult: LiveData<Result<VideoSearchResult>> = super._searchResult

    override fun fetchSearchResult(
        query: String,
        pageToken: String,
        subtitles: Boolean,
        maxResults: Int
    ) {
        if (!super.isSearchResultLoading) {
            viewModelScope.launch {
                super.isSearchResultLoading = true
                try {
                    val result = netRepository.getSearchResult(query, pageToken, maxResults, subtitles).await()
                    result.onSuccess { res: VideoSearchResult ->

                        this@VideoPlayerViewModel.searchQuery = query
                        this@VideoPlayerViewModel.pageToken = pageToken

                        _selectedVideo.value?.onSuccess { item: VideoItem ->
                            val modifiedResult = res.copy(videoItems = res.videoItems.filter { it.id.videoId != item.id.videoId })
                            super._searchResult.value = Result.success(modifiedResult)
                        }
                    }
                    result.onFailure { exception ->
                        super._searchResult.value = Result.failure(exception)
                    }
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                    super._searchResult.value = Result.failure(e)
                } finally {
                    super.isSearchResultLoading = false
                }
            }
        }
    }

    private var _selectedVideo = MutableLiveData<Result<VideoItem>>()
    val selectedVideo: LiveData<Result<VideoItem>> = _selectedVideo

    fun setSelectedVideo(videoItem: VideoItem) {
        _selectedVideo.value = Result.success(videoItem)
    }

    var volume = MutableLiveData(100)
    var videoProgress = 0
    var isVideoProgressManualChanged = false
    var duration = Float.MAX_VALUE
    var currentSecond: Float = 0f

    fun getProgressInPercentages(second: Float): Int {
        val progress = (100 / this.duration * second).toInt()
        return progress
    }

    fun getProgressInSeconds(progress: Int): Float {
        val second = (progress * this.duration) / 100
        return second - 1
    }

    fun saveSelectedVideoToHistory(
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
        onSuccess: (id: Long) -> Unit = {},
        onFailure: (e: Exception) -> Unit = {}
    ) {
        onStart.invoke()
        val videoItem = _selectedVideo.value?.getOrNull()
        videoItem?.let { item ->
            val historyQuery = HistoryQuery(
                videoId = item.id.videoId,
                viewingTime = System.currentTimeMillis(),
                text = item.snippet.title,
                thumbnailUrl = item.snippet.thumbnails.default.url,
                pageToken = this.pageToken
            )
            viewModelScope.launch {
                try {
                    val result = dbRepository.addVideoToHistory(historyQuery).await()
                    result.onSuccess { value: Long ->
                        onSuccess.invoke(value)
                    }
                    result.onFailure { exception ->
                        onFailure.invoke(exception as Exception)
                    }
                } finally {
                    onComplete.invoke()
                }
            }
        }?: run {
            onComplete.invoke()
        }
    }


}