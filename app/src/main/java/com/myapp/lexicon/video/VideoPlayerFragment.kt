package com.myapp.lexicon.video

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
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
import com.myapp.lexicon.video.models.captions.CaptionList
import kotlinx.coroutines.launch

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

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

}