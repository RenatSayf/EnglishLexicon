package com.myapp.lexicon.video

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoPlayerViewModel(
    private val videoId: String?
) : ViewModel() {

    class Factory(private val videoId: String?): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == VideoPlayerViewModel::class.java)
            return VideoPlayerViewModel(videoId = this.videoId) as T
        }
    }

    private val youTubeBaseUrl = "https://www.youtube.com/watch"

    fun getVideoUri(): Uri {
        return Uri.parse("$youTubeBaseUrl?v=$videoId")
    }


}