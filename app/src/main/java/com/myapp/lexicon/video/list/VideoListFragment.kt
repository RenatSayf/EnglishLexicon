@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video.list

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentVideoListBinding
import com.myapp.lexicon.helpers.showToastIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.repository.network.NetRepository
import com.myapp.lexicon.video.models.VideoSearchResult

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = VideoListViewModel.Factory(repository!!)
        videoListVM = ViewModelProvider(this, factory)[VideoListViewModel::class.java]

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