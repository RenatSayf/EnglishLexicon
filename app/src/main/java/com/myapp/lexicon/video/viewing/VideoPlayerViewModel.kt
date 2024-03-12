@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen", "UnnecessaryVariable")

package com.myapp.lexicon.video.viewing

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

    var volume = MutableLiveData(100)

    fun saveSelectedVideoToHistory(
        video: VideoItem,
        onStart: () -> Unit = {},
        onComplete: (t: Throwable?) -> Unit = {},
        onSuccess: (id: Long) -> Unit = {}
    ) {
        onStart.invoke()

        try {
            val historyQuery = HistoryQuery(
                videoId = video.id.videoId,
                viewingTime = System.currentTimeMillis(),
                text = video.snippet.title,
                thumbnailUrl = video.snippet.thumbnails.default.url,
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
        } finally {
            onComplete.invoke(null)
        }
    }

    var screenState: State = State()
        private set
    fun resetScreenState() {
        screenState = State()
    }

    data class State(var stateId: Long = System.currentTimeMillis()) {

        var player: Player = Player()
        class Player {
            var videoId: String = ""
                get() {
                    return field.ifEmpty { throw IllegalStateException("******** Property videoId must be set primarily *********") }
                }
            var currentSecond: Float = 0f
            var error: PlayerConstants.PlayerError? = null
            var playbackQuality: PlayerConstants.PlaybackQuality = PlayerConstants.PlaybackQuality.DEFAULT
            var playbackRate: PlayerConstants.PlaybackRate = PlayerConstants.PlaybackRate.RATE_1
            var state: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNSTARTED
            var duration: Float = Float.MAX_VALUE
            var progress: Int = getProgressInPercentages(currentSecond)
            var volume: Int = 100
            var isVideoProgressManualChanged = false

            fun getProgressInPercentages(second: Float): Int {
                val progress = (100 / this.duration * second).toInt()
                return progress
            }

            fun getProgressInSeconds(progress: Int): Float {
                val second = (progress * this.duration) / 100
                return second - 1
            }
        }
    }


}