@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video

import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentVideoPlayerBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.repository.network.MockNetRepository
import com.myapp.lexicon.video.list.VideoListAdapter
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.VideoSearchResult
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.serialization.json.Json

class VideoPlayerFragment : Fragment() {

    companion object {

        val TAG = "${VideoPlayerViewModel::class.simpleName}.tag358855"
        const val ARG_VIDEO_ITEM = "ARG_VIDEO_ID"
        const val ARG_SEARCH_RESULT = "ARG_SEARCH_RESULT"
        fun newInstance() = VideoPlayerFragment()
    }

    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var playerVM: VideoPlayerViewModel
    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    private val videoListAdapter: VideoListAdapter by lazy {
        VideoListAdapter.getInstance()
    }

    private var youTubePlayer: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locker.lock()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoItemStr = arguments?.getString(ARG_VIDEO_ITEM)
        if (videoItemStr == null) {
            showSnackBar("Видео недоступно ${getString(R.string.confused_face)}")
            parentFragmentManager.popBackStack()
            Exception("******** videoItemStr is NULL **********").throwIfDebug()
        }
        val videoItem = try {
            Json.decodeFromString<VideoItem>(videoItemStr!!)
        } catch (e: Exception) {
            e.throwIfDebug()
            null
        }
        videoItem?.let {
            val factory = VideoPlayerViewModel.Factory(repository = MockNetRepository())
            playerVM = ViewModelProvider(this, factory)[VideoPlayerViewModel::class.java]
            playerVM.setSelectedVideo(it)
        }?: run {
            showSnackBar("Видео недоступно ${getString(R.string.confused_face)}")
            parentFragmentManager.popBackStack()
            Exception("******** videoItem is NULL **********").throwIfDebug()
        }

        val searchResultStr = arguments?.getString(ARG_SEARCH_RESULT)
        searchResultStr?.let {
            try {
                val searchResult = Json.decodeFromString<VideoSearchResult>(it)
                playerVM.setSearchResult(searchResult)
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
            }
        }

        with(binding) {

            rvVideoList.apply {
                adapter = videoListAdapter

                setOnScrollChangeListener(object : View.OnScrollChangeListener {
                    override fun onScrollChange(view: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                        when {
                            oldScrollY > scrollY -> {
                                layoutControlPane.changeTopMarginAnimatedly(0)
                            }
                            else -> {
                                if (oldScrollY != scrollY) {
                                    layoutControlPane.changeTopMarginAnimatedly()
                                }
                            }
                        }
                    }
                })
            }

            playerVM.selectedVideo.observe(viewLifecycleOwner) { result ->
                result.onSuccess { value: VideoItem ->

                    val thumbnailUri = Uri.parse(value.snippet.thumbnails.high.url)
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
                    tvTitle.text = value.snippet.title
                    videoPlayerInitialize()
                }
                result.onFailure { exception ->
                    exception.throwIfDebug()
                }
            }

            playerVM.videoCued.observe(viewLifecycleOwner) { value ->
                if (value) {
                    ivPlaceHolder.visibility = View.GONE
                    groupPlayerControl.visibility = View.VISIBLE
                    btnPause.visibility = View.INVISIBLE
                }
                else {
                    ivPlaceHolder.visibility = View.VISIBLE
                    groupPlayerControl.visibility = View.GONE
                    seekbarVideo.visibility = View.GONE
                }

            }

            playerVM.isPlaying.observe(viewLifecycleOwner) { value ->
                when (value) {
                    true -> {
                        btnPlay.visibility = View.INVISIBLE
                        btnPause.visibility = View.VISIBLE
                        seekbarVideo.visibility = View.VISIBLE
                    }
                    null -> {
                        return@observe
                    }
                    else -> {
                        if (groupPlayerControl.isVisible) {
                            btnPlay.visibility = View.VISIBLE
                            btnPause.visibility = View.INVISIBLE
                        }
                    }
                }
            }

            playerVM.volume.observe(viewLifecycleOwner) { value ->
                seekBarSound.progress = value
                when(value) {
                    in 0..10 -> {
                        btnSoundOff.setImageResource(R.drawable.ic_volume_mute)
                    }
                    else -> {
                        btnSoundOff.setImageResource(R.drawable.ic_volume_up)
                    }
                }
                youTubePlayer?.setVolume(value)
            }

            playerVM.videoProgress.observe(viewLifecycleOwner) { value ->
                if (!playerVM.isVideoProgressManualChanged) {
                    seekbarVideo.progress = value
                }
            }

            playerVM.currentSecond.observe(viewLifecycleOwner) { value ->
                playerVM.videoProgress.value = playerVM.getProgressInPercentages(value)
            }

            playerVM.searchResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess { value: VideoSearchResult ->
                    videoListAdapter.submitList(value.videoItems)
                }
            }

            btnPlay.setOnClickListener {
                youTubePlayer?.play()
            }
            btnPause.setOnClickListener {
                youTubePlayer?.pause()
            }

            seekbarVideo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                    playerVM.isVideoProgressManualChanged = p2
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val progress = seekBar?.progress
                    if (progress != null) {
                        youTubePlayer?.seekTo(playerVM.getProgressInSeconds(progress))
                    }
                }
            })
            seekBarSound.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    val progress = p0?.progress
                    if (progress != null) {
                        playerVM.volume.value = progress
                    }
                }
            })
            btnSoundOff.setOnClickListener {
                val volume = seekBarSound.progress
                if (volume > 0) {
                    playerVM.volume.value = 0
                }
                else {
                    playerVM.volume.value = 100
                }
            }


        }
    }

    private fun FragmentVideoPlayerBinding.videoPlayerInitialize() {
        val playerOptions = IFramePlayerOptions.Builder().apply {
            controls(0)
            ccLoadPolicy(1)
        }.build()
        viewLifecycleOwner.lifecycle.addObserver(playerView)
        playerView.initialize(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {}

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                playerVM.currentSecond.value = second
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
                this@VideoPlayerFragment.youTubePlayer = youTubePlayer
                val result = playerVM.selectedVideo.value
                result?.onSuccess { value: VideoItem ->
                    youTubePlayer.cueVideo(value.id.videoId, playerVM.videoTimeMarker)
                } ?: run {
                    Exception("****** Result.VideoItem is NULL **********").throwIfDebug()
                }
                result?.onFailure { tr ->
                    tr.throwIfDebug()
                }
                playerView
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                when (state) {
                    PlayerConstants.PlayerState.VIDEO_CUED -> {
                        playerVM.setVideoCued(true)
                    }

                    PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.BUFFERING -> {
                        playerVM.setPlaying(true)
                    }

                    PlayerConstants.PlayerState.PAUSED -> {
                        playerVM.setPlaying(false)
                    }

                    else -> {}
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                playerVM.duration.value = duration
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
            }
        }, true, playerOptions)
    }

    fun ConstraintLayout.changeTopMarginAnimatedly(value: Int = -this.height) {
        if (this.id == R.id.layoutControlPane) {
            val layoutParams = this@changeTopMarginAnimatedly.layoutParams as ConstraintLayout.LayoutParams
            val initBottomMargin = layoutParams.topMargin
            ValueAnimator.ofInt(initBottomMargin, value).apply {
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animator: ValueAnimator) {
                        layoutParams.topMargin = animator.animatedValue as Int
                        this@changeTopMarginAnimatedly.layoutParams = layoutParams
                    }
                })
            }.setDuration(300).start()
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    override fun onDestroy() {

        binding.playerView.release()
        locker.unLock()

        super.onDestroy()
    }

}