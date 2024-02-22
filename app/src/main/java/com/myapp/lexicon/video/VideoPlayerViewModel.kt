@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen", "UnnecessaryVariable")

package com.myapp.lexicon.video

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoPlayerViewModel(
    private val app: Application,
    private val repository: INetRepository
) : AndroidViewModel(app) {

    class Factory(
        private val repository: INetRepository = NetRepositoryModule.provideNetRepository()
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoPlayerViewModel::class.java)
            return VideoPlayerViewModel(app = App.INSTANCE, repository = this.repository) as T
        }
    }

    var videoTimeMarker: Float = 0.0f

    private var _searchResult = MutableLiveData<Result<VideoSearchResult>>()
    val searchResult: LiveData<Result<VideoSearchResult>> = _searchResult

    fun setSearchResult(result: VideoSearchResult) {
        _selectedVideo.value?.onSuccess { value: VideoItem ->
            val modifiedResult = result.copy(videoItems = result.videoItems.filter { it.id.videoId != value.id.videoId })
            _searchResult.value = Result.success(modifiedResult)
        }?: run {
            Exception("******** _selectedVideo.value is NULL ***************").throwIfDebug()
        }
    }

    fun getSearchResult() {

    }

    private var _selectedVideo = MutableLiveData<Result<VideoItem>>()
    val selectedVideo: LiveData<Result<VideoItem>> = _selectedVideo

    fun setSelectedVideo(videoItem: VideoItem) {
        _selectedVideo.value = Result.success(videoItem)
    }

    private var _videoCued = MutableLiveData(false)
    val videoCued: LiveData<Boolean> = _videoCued

    fun setVideoCued(value: Boolean) {
        _videoCued.value = value
    }

    private var _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun setPlaying(value: Boolean) {
        _isPlaying.value = value
    }

    var volume = MutableLiveData(100)
    var videoProgress = MutableLiveData(0)
    var isVideoProgressManualChanged = false
    var duration = MutableLiveData(Float.MAX_VALUE)
    var currentSecond = MutableLiveData(0f)

    fun getProgressInPercentages(second: Float): Int {
        val progress = (100 / this.duration.value!! * second).toInt()
        return progress
    }

    fun getProgressInSeconds(progress: Int): Float {
        val second = (progress * this.duration.value!!) / 100
        return second - 1
    }


}