package com.myapp.lexicon.video.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.ItemVideoBinding
import com.myapp.lexicon.video.models.VideoItem
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class VideoListAdapter private constructor(): ListAdapter<VideoItem, VideoListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.id.videoId == newItem.id.videoId
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.id.videoId == newItem.id.videoId && oldItem.snippet.description == newItem.snippet.description
        }
    }
){
    companion object {
        private var instance: VideoListAdapter? = null
        fun getInstance(): VideoListAdapter {
            return if (instance == null) {
                instance = VideoListAdapter()
                instance!!
            }
            else instance!!
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VideoItem) {
            with(binding as ItemVideoBinding) {

                val thumbnailUri = Uri.parse(item.snippet.thumbnails.high.url)
                Picasso.get().load(thumbnailUri)
                    .placeholder(R.drawable.ic_smart_display)
                    .into(ivPlaceHolder, object : Callback {
                        override fun onSuccess() {
                            progressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            progressBar.visibility = View.GONE
                        }
                    })

                tvTitle.text = item.snippet.title
                tvTitle.tag = item.id.videoId
                tvDescription.text = item.snippet.description
                tvDate.text = item.snippet.publishTime

                playerView.addYouTubePlayerListener(object : YouTubePlayerListener {
                    override fun onApiChange(youTubePlayer: YouTubePlayer) {

                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {

                    }

                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError
                    ) {

                    }

                    override fun onPlaybackQualityChange(
                        youTubePlayer: YouTubePlayer,
                        playbackQuality: PlayerConstants.PlaybackQuality
                    ) {

                    }

                    override fun onPlaybackRateChange(
                        youTubePlayer: YouTubePlayer,
                        playbackRate: PlayerConstants.PlaybackRate
                    ) {

                    }

                    override fun onReady(youTubePlayer: YouTubePlayer) {

                    }

                    override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        if (state == PlayerConstants.PlayerState.PLAYING) {
                            ivPlaceHolder.visibility = View.GONE
                            playerView.visibility = View.VISIBLE
                        }
                    }

                    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {

                    }

                    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {

                    }

                    override fun onVideoLoadedFraction(
                        youTubePlayer: YouTubePlayer,
                        loadedFraction: Float
                    ) {

                    }

                })

            }
        }
    }


}