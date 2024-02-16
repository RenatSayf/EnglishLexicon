package com.myapp.lexicon.video

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentVideoPlayerBinding
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.video.models.VideoItem
import com.myapp.lexicon.video.models.captions.CaptionList
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class VideoPlayerFragment : Fragment() {

    companion object {

        val TAG = "${VideoPlayerViewModel::class.simpleName}.tag358855"
        const val ARG_VIDEO_ITEM = "ARG_VIDEO_ID"
        fun newInstance() = VideoPlayerFragment()
    }

    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var playerVM: VideoPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoItemStr = arguments?.getString(ARG_VIDEO_ITEM)
        if (videoItemStr == null) {
            showSnackBar("Видео недоступно ${getString(R.string.confused_face)}")
            parentFragmentManager.popBackStack()
        }
        val videoItem = Json.decodeFromString<VideoItem>(videoItemStr!!)
        val factory = VideoPlayerViewModel.Factory(videoItem.id.videoId)
        playerVM = ViewModelProvider(this, factory)[VideoPlayerViewModel::class.java]

        with(binding) {

            btnGetSubtitles.setOnClickListener {
                val accountIntent =
                    AccountPicker.newChooseAccountIntent(
                        AccountPicker.AccountChooserOptions.Builder()
                            .apply {
                                this.setAlwaysShowAccountPicker(true)
                                setAllowableAccountsTypes(
                                    listOf(
                                        GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE
                                    )
                                )
                            }.build()
                    )
                this@VideoPlayerFragment.startActivityForResult(accountIntent, 9933)
            }

            val thumbnailUri = Uri.parse(videoItem.snippet.thumbnails.high.url)
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

            playerVM.authToken.observe(viewLifecycleOwner) { result ->
                result.onSuccess { token: String ->
                    playerVM.getCaptionsList(token)
                    showSnackBar("Token successful")
                }
                result.onFailure { exception ->
                    printLogIfDebug(exception.message?: getString(R.string.text_unknown_error_message))
                    showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                }
            }

            lifecycleScope.launch {
                playerVM.captionListResult.collect(collector = { result ->
                    result.onSuccess { list: CaptionList ->
                        if (list.items.isNotEmpty()) {
                            val captionsId = list.items.find { it.snippet.language == "en" }?.id
                            if (!captionsId.isNullOrEmpty()) {
                                val authToken = list.authToken
                                playerVM.loadCaptions(captionsId, authToken)
                            }
                            else {
                                showSnackBar("Caption id is NULL")
                            }
                        }
                    }
                    result.onFailure { exception ->
                        val message = exception.message ?: getString(R.string.text_unknown_error_message)
                        showSnackBar(message)
                    }
                })
            }

            val playerOptions = IFramePlayerOptions.Builder().apply {
                controls(1)
                ccLoadPolicy(1)
            }.build()
            viewLifecycleOwner.lifecycle.addObserver(playerView)
            playerView.initialize(object : YouTubePlayerListener {
                override fun onApiChange(youTubePlayer: YouTubePlayer) {}

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    playerVM.videoTimeMarker = second
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {

                }

                override fun onPlaybackQualityChange(
                    youTubePlayer: YouTubePlayer,
                    playbackQuality: PlayerConstants.PlaybackQuality
                ) {}

                override fun onPlaybackRateChange(
                    youTubePlayer: YouTubePlayer,
                    playbackRate: PlayerConstants.PlaybackRate
                ) {

                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    playerVM.videoId?.let { id ->
                        youTubePlayer.cueVideo(id, playerVM.videoTimeMarker)
                    }?: run {
                        printLogIfDebug("****** Video ID is NULL **********")
                    }
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    if (state == PlayerConstants.PlayerState.VIDEO_CUED) {
                        ivPlaceHolder.visibility = View.GONE
                    }
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {

                }

                override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

                override fun onVideoLoadedFraction(
                    youTubePlayer: YouTubePlayer,
                    loadedFraction: Float
                ) {}
            }, true, playerOptions)

        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9933) {
            val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val account = Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            playerVM.getAuthToken(account)
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
        super.onDestroy()
    }

}