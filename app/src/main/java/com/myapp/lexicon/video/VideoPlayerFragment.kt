package com.myapp.lexicon.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.databinding.FragmentVideoPlayerBinding

class VideoPlayerFragment : Fragment() {

    companion object {

        val TAG = "${VideoPlayerViewModel::class.simpleName}.tag358855"
        const val ARG_VIDEO_ID = "ARG_VIDEO_ID"
        fun newInstance() = VideoPlayerFragment()
    }

    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var playerVM: VideoPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoId = arguments?.getString(ARG_VIDEO_ID)
        val factory = VideoPlayerViewModel.Factory(videoId)
        playerVM = ViewModelProvider(this, factory)[VideoPlayerViewModel::class.java]
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

        with(binding) {


        }
    }

}