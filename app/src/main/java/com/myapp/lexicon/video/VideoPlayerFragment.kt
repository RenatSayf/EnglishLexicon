@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video

import android.animation.ValueAnimator
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentVideoPlayerBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.checkOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.video.list.VideoListAdapter
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.search.SearchFragment
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
        const val ARG_SEARCH_QUERY = "ARG_SEARCH_RESULT"
        const val ARG_PAGE_TOKEN = "ARG_PAGE_TOKEN"
        const val KEY_CALLBACK_REQUEST = "KEY_CALLBACK_REQUEST"

        fun newInstance() = VideoPlayerFragment()
    }

    private var binding: FragmentVideoPlayerBinding? = null
    private lateinit var playerVM: VideoPlayerViewModel
    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    private val videoListAdapter: VideoListAdapter by lazy {
        VideoListAdapter.getInstance()
    }

    private val jsonDecoder: Json by lazy {
        Json { ignoreUnknownKeys }
    }

    private var youTubePlayer: YouTubePlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoItemStr = arguments?.getString(ARG_VIDEO_ITEM)
        if (videoItemStr == null) {
            showSnackBar(
                getString(
                    R.string.text_video_unavailable,
                    getString(R.string.confused_face)
                ))
            parentFragmentManager.popBackStack()
            Exception("******** videoItemStr is NULL **********").throwIfDebug()
        }
        val videoItem = try {
            jsonDecoder.decodeFromString<VideoItem>(videoItemStr!!)
        } catch (e: Exception) {
            e.throwIfDebug()
            null
        }
        videoItem?.let {
            val factory = VideoPlayerViewModel.Factory()
            playerVM = ViewModelProvider(requireActivity(), factory)[VideoPlayerViewModel::class.java]
            playerVM.setSelectedVideo(it)
        }?: run {
            showSnackBar("Видео недоступно ${getString(R.string.confused_face)}")
            parentFragmentManager.popBackStack()
            Exception("******** videoItem is NULL **********").throwIfDebug()
        }

        with(binding!!) {

            if (savedInstanceState != null) {
                restoreScreenState(playerVM.screenState)
            }

            rvVideoList.apply {
                videoListAdapter.setItemClickCallback { videoItem: VideoItem ->
                    playerVM.setSelectedVideo(videoItem)
                }
                adapter = videoListAdapter

                setOnScrollChangeListener(object : View.OnScrollChangeListener {
                    override fun onScrollChange(view: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                        when {
                            oldScrollY > scrollY -> {
                                layoutControlPane.changeTopMarginAnimatedly(0)
                                bottomBar.changeBottomMarginAnimatedly(0)
                            }
                            else -> {
                                if (oldScrollY != scrollY) {
                                    layoutControlPane.changeTopMarginAnimatedly()
                                    bottomBar.changeBottomMarginAnimatedly(-bottomBar.height)
                                }
                            }
                        }
                    }
                })

                addOnScrollListener(object : OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                        val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                        if (lastVisibleItemPosition == videoListAdapter.itemCount - 1) {
                            val searchResult = playerVM.searchResult.value?.getOrNull()
                            if (searchResult != null) {
                                playerVM.fetchSearchResult(query = searchResult.query, pageToken = searchResult.nextPageToken)
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

                    val searchQuery = arguments?.getString(ARG_SEARCH_QUERY)
                    searchQuery?.let {
                        playerVM.fetchSearchResult(
                            query = it,
                            pageToken = arguments?.getString(ARG_PAGE_TOKEN)?: ""
                        )
                    }

                    if (youTubePlayer == null) {
                        videoPlayerInitialize()
                    }
                    else {
                        youTubePlayer?.cueVideo(value.id.videoId, 0f)
                        seekbarVideo.progress = 0
                        playerVM.currentSecond = 0f
                    }
                }
                result.onFailure { exception ->
                    exception.throwIfDebug()
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
                playerVM.screenState.player.volume = value
            }

            playerVM.searchResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess { value: VideoSearchResult ->
                    if (value.prevPageToken == null) {
                        videoListAdapter.submitList(value.videoItems)
                    } else {
                        videoListAdapter.addItemsToCurrentList(value.videoItems)
                    }
                }
                result.onFailure { exception ->
                    exception.printStackTraceIfDebug()
                }
            }

            btnPlay.setOnClickListener {
                youTubePlayer?.play()
            }
            btnPause.setOnClickListener {
                youTubePlayer?.pause()
            }

            btnStepBack.setOnClickListener {
                skipVideoOn(-5f)
            }

            btnStepForward.setOnClickListener {
                skipVideoOn(5f)
            }
            btnFastBack.setOnClickListener {
                skipVideoOn(-20f)
            }

            btnFastForward.setOnClickListener {
                skipVideoOn(20f)
            }

            seekbarVideo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                    playerVM.isVideoProgressManualChanged = p2
                    playerVM.videoProgress = progress
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

            btnSearch.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .add(R.id.frame_to_page_fragm, SearchFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }

            btnFullScreen.setOnClickListener {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

        }
    }

    private fun skipVideoOn(seconds: Float) {
        val currentSecond = playerVM.currentSecond
        youTubePlayer?.seekTo(currentSecond + seconds)
    }

    private fun FragmentVideoPlayerBinding.videoPlayerInitialize() {
        val playerOptions = IFramePlayerOptions.Builder().apply {
            controls(0)
            ccLoadPolicy(1)
            fullscreen(0)
        }.build()
        viewLifecycleOwner.lifecycle.addObserver(playerView)
        playerView.initialize(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {}

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                if (!playerVM.isVideoProgressManualChanged) {
                    val progressInPercentages = playerVM.getProgressInPercentages(second)
                    seekbarVideo.progress = progressInPercentages
                    playerVM.currentSecond = second

                    playerVM.screenState.player.currentSecond = second

                    if (progressInPercentages > 5) {
                        playerVM.saveSelectedVideoToHistory(
                            onStart = { locker.lock() },
                            onComplete = { locker.unLock() }
                        )
                    }
                }
            }

            override fun onError(
                youTubePlayer: YouTubePlayer,
                error: PlayerConstants.PlayerError
            ) {
                Exception(error.name).printStackTraceIfDebug()
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
                playerVM.screenState.player.playbackQuality = playbackQuality
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
                playerVM.screenState.player.playbackRate = playbackRate
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@VideoPlayerFragment.youTubePlayer = youTubePlayer
                val result = playerVM.selectedVideo.value
                result?.onSuccess { value: VideoItem ->
                    youTubePlayer.cueVideo(value.id.videoId, playerVM.videoTimeMarker)
                    seekbarVideo.progress = playerVM.getProgressInPercentages(playerVM.videoTimeMarker)
                } ?: run {
                    Exception("****** Result.VideoItem is NULL **********").throwIfDebug()
                }
                result?.onFailure { tr ->
                    tr.throwIfDebug()
                }
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                when (state) {
                    PlayerConstants.PlayerState.VIDEO_CUED -> {
                        seekbarVideo.visibility = View.VISIBLE
                        ivPlaceHolder.visibility = View.GONE
                        groupPlayerControl.visibility = View.VISIBLE
                        btnPause.visibility = View.INVISIBLE
                    }
                    PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.BUFFERING -> {
                        btnPlay.visibility = View.INVISIBLE
                        btnPause.visibility = View.VISIBLE
                    }
                    PlayerConstants.PlayerState.PAUSED -> {

                        btnPlay.visibility = View.VISIBLE
                        btnPause.visibility = View.INVISIBLE
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        ivPlaceHolder.visibility = View.VISIBLE
                    }
                    else -> {}
                }
                playerVM.screenState.player.state = state
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                playerVM.duration = duration
                playerVM.screenState.player.duration = duration
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                return
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
                return
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

    fun LinearLayoutCompat.changeBottomMarginAnimatedly(value: Int = this.marginBottom) {
        val layoutParams = this@changeBottomMarginAnimatedly.layoutParams as ConstraintLayout.LayoutParams
        val paramToAnimate = layoutParams.bottomMargin
        ValueAnimator.ofInt((paramToAnimate), value).apply {
            addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animator: ValueAnimator) {
                    layoutParams.bottomMargin = animator.animatedValue as Int
                    this@changeBottomMarginAnimatedly.layoutParams = layoutParams
                }
            })
        }.setDuration(300).start()
    }

    private fun FragmentVideoPlayerBinding.restoreScreenState(state: VideoPlayerViewModel.State) {
        val player = state.player
        when(player.state) {
            PlayerConstants.PlayerState.ENDED -> {
                ivPlaceHolder.visibility = View.VISIBLE
            }
            PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.BUFFERING -> {
                btnPlay.visibility = View.INVISIBLE
                btnPause.visibility = View.VISIBLE
                youTubePlayer?.apply {
                    seekTo(state.player.currentSecond)
                    play()
                }
            }
            PlayerConstants.PlayerState.PAUSED -> {
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
                youTubePlayer?.apply {
                    seekTo(state.player.currentSecond)
                    pause()
                }
            }
            PlayerConstants.PlayerState.VIDEO_CUED -> {
                seekbarVideo.visibility = View.VISIBLE
                ivPlaceHolder.visibility = View.GONE
                groupPlayerControl.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            }
            else -> {}
        }
        youTubePlayer?.apply {
            setVolume(player.volume)
            seekBarSound.progress = player.volume
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireContext().checkOrientation(
                    onLandscape = {
                        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    },
                    onPortrait = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        })

        binding?.btnBack?.setOnClickListener {
            requireContext().checkOrientation(
                onLandscape = {
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                },
                onPortrait = {
                    parentFragmentManager.popBackStack()
                }
            )
        }

        setFragmentResult(KEY_CALLBACK_REQUEST, Bundle().apply {

        })
    }

    override fun onDestroy() {

        binding?.playerView?.release()
        super.onDestroy()
    }

}