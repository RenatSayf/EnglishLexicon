package com.myapp.lexicon.video.viewing.fullscreen

import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar.LayoutParams
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentPlayerFullScreenBinding
import com.myapp.lexicon.video.viewing.VideoPlayerFragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class PlayerFullScreenFragment : Fragment() {

    companion object {
        fun newInstance() = PlayerFullScreenFragment()
    }

    private var thisBinding: FragmentPlayerFullScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        thisBinding = FragmentPlayerFullScreenBinding.inflate(inflater, container, false)
        return thisBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(thisBinding!!) {


        }
    }

    override fun onDestroy() {

        thisBinding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onDestroy()
    }

}