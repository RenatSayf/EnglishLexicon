@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen", "UnnecessaryVariable")

package com.myapp.lexicon.video.viewing

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
import com.myapp.lexicon.video.list.VideoListViewModel
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.query.HistoryQuery
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import kotlinx.coroutines.launch

class VideoPlayerViewModel(
    private val netRepository: INetRepository,
    private val dbRepository: IDataRepository
) : VideoListViewModel(netRepository, dbRepository) {

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

    var searchQuery: String = ""
        private set
    var pageToken: String = ""
        private set

    private var _selectedVideo = MutableLiveData<Result<VideoItem>>()
    val selectedVideo: LiveData<Result<VideoItem>> = _selectedVideo

    fun setSelectedVideo(videoItem: VideoItem) {
        _selectedVideo.value = Result.success(videoItem)
        screenState.videoId = videoItem.id.videoId
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
        onComplete: (t: Throwable?) -> Unit = {},
        onSuccess: (id: Long) -> Unit = {}
    ) {
        onStart.invoke()
        val videoItem = _selectedVideo.value?.getOrNull()
        videoItem?.let { item ->
            val historyQuery = HistoryQuery(
                videoId = item.id.videoId,
                viewingTime = System.currentTimeMillis(),
                text = item.snippet.title,
                thumbnailUrl = item.snippet.thumbnails.default.url,
                pageToken = this.pageToken,
                searchQuery = this.searchQuery
            )
            viewModelScope.launch {
                val result = dbRepository.addVideoToHistory(historyQuery).await()
                result.onSuccess { value: Long ->
                    onSuccess.invoke(value)
                }
                result.onFailure { exception ->
                    onComplete.invoke(exception as Exception)
                }
            }
        }?: run {
            onComplete.invoke(null)
        }
    }

    val screenState = State("")

    data class State(var videoId: String) {

        var player: Player = Player()
        class Player {
            var isInit = false
            var currentSecond: Float = 0f
            var error: PlayerConstants.PlayerError? = null
            var playbackQuality: PlayerConstants.PlaybackQuality = PlayerConstants.PlaybackQuality.DEFAULT
            var playbackRate: PlayerConstants.PlaybackRate = PlayerConstants.PlaybackRate.RATE_1
            var state: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNSTARTED
            var duration: Float = Float.MAX_VALUE
            var progress: Int = getProgressInPercentages(currentSecond)
            var volume: Int = 100

            private fun getProgressInPercentages(second: Float): Int {
                val progress = (100 / this.duration * second).toInt()
                return progress
            }
        }
    }


}