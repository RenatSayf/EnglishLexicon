@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

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
        _screenState.value = _screenState.value?.copy(videoId = videoItem.id.videoId)
    }

    private var _screenState = MutableLiveData<ScreenState>().apply {
        value = ScreenState.getInstance()
    }
    val screenState: LiveData<ScreenState> = _screenState

    fun setScreenState(state: ScreenState?) {
        state?.let {
            _screenState.value = it
        }
    }

    @Suppress("DataClassPrivateConstructor")
    data class ScreenState private constructor(
        var videoId: String = "",
        var isPlay: Boolean = false,
        var volume: Int = 100,
        var videoProgress: Float = 0f,
        var duration: Float = 0f
    ) {
        companion object {

            private var instance: ScreenState? = null
            fun getInstance(): ScreenState {
                return if (instance == null) {
                    instance = ScreenState()
                    instance!!
                }
                else instance!!
            }
        }
    }

}