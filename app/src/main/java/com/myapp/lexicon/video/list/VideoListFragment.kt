@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video.list

import android.accounts.Account
import android.accounts.AccountManager
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentVideoListBinding
import com.myapp.lexicon.helpers.showToastIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.repository.network.NetRepository
import com.myapp.lexicon.video.VideoPlayerViewModel
import com.myapp.lexicon.video.models.VideoSearchResult
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Suppress("DEPRECATION")
class VideoListFragment private constructor(): Fragment() {

    companion object {

        private var repository: INetRepository? = null
        fun newInstance(repository: INetRepository = NetRepository()): VideoListFragment {

            this.repository = repository
            return VideoListFragment()
        }
    }

    private var binding: FragmentVideoListBinding? = null
    private lateinit var videoListVM: VideoListViewModel
    private val videoListAdapter: VideoListAdapter by lazy {
        VideoListAdapter.getInstance()
    }
    private lateinit var videoPlayerVM: VideoPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listFactory = VideoListViewModel.Factory(repository!!)
        videoListVM = ViewModelProvider(this, listFactory)[VideoListViewModel::class.java]

        val playerFactory = VideoPlayerViewModel.Factory("")
        videoPlayerVM = ViewModelProvider(this, playerFactory)[VideoPlayerViewModel::class.java]

        with(binding!!) {

            rvVideoList.apply {
                adapter = videoListAdapter
                setOnScrollChangeListener(object : View.OnScrollChangeListener {
                    override fun onScrollChange(view: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {

                        when {
                            oldScrollY > scrollY -> {
                                bottomBar.hideAnimate(0)
                            }
                            else -> {
                                if (oldScrollY != scrollY) {
                                    val height = (bottomBar.layoutParams as ConstraintLayout.LayoutParams).height
                                    bottomBar.hideAnimate(-height)
                                }
                            }
                        }
                    }
                })

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                        val views = recyclerView.children.toList()
                        if (views.isNotEmpty()) {
                            val playerView = views[0].findViewById<YouTubePlayerView>(R.id.playerView)
                            val videoId = views[0].findViewById<TextView>(R.id.tvTitle).tag.toString()
                            playerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                    youTubePlayer.loadVideo(videoId, 0.0f)

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
                                    this@VideoListFragment.startActivityForResult(accountIntent, 9933)
                                }
                            })
                        }
                    }
                })

            }

            videoListVM.searchResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess { value: VideoSearchResult ->
                    videoListAdapter.submitList(value.videoItems)
                }
                result.onFailure { exception ->
                    showToastIfDebug(exception.message)
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9933) {
            val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val account = Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            videoPlayerVM.videoId?.let { videoPlayerVM.getAuthToken(it, account) } //TODO videoId is empty
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
        with(binding!!) {
            btnBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    fun LinearLayoutCompat.hideAnimate(value: Int) {
        if (this.id == R.id.bottomBar) {
            val layoutParams = this@hideAnimate.layoutParams as ConstraintLayout.LayoutParams
            val initBottomMargin = layoutParams.bottomMargin
            ValueAnimator.ofInt(initBottomMargin, value).apply {
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animator: ValueAnimator) {
                        layoutParams.bottomMargin = animator.animatedValue as Int
                        this@hideAnimate.layoutParams = layoutParams
                    }
                })
            }.setDuration(300).start()
        }
    }

}