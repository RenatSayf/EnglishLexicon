@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.video.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdFragment
import com.myapp.lexicon.common.MOBILE_YOUTUBE_URL
import com.myapp.lexicon.databinding.FragmentYouTubeBinding
import java.util.concurrent.TimeUnit


private const val WEB_VIEW_BUNDLE = "WEB_VIEW_BUNDLE"

class YouTubeFragment : Fragment() {
    companion object {

        const val KEY_AD_DISMISSED = "KEY_AD_DISMISSED_2548"
        const val KEY_AD_LOADED = "KEY_AD_LOADED_52347"

        @JvmStatic
        fun newInstance() = YouTubeFragment()
    }

    private var binding: FragmentYouTubeBinding? = null

    private val youTubeVM: YouTubeViewModel by lazy {
        ViewModelProvider(this)[YouTubeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            youTubeVM.startAdTimer(TimeUnit.MINUTES.toMillis(1))
            parentFragmentManager.beginTransaction().replace(R.id.fragmentAdLayout, AdFragment.newInstance()).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYouTubeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            webView.apply {
                canGoForward()
                canGoBack()
                settings.apply {
                    javaScriptEnabled = true
                    setSupportMultipleWindows(true)
                    javaScriptCanOpenWindowsAutomatically = true
                    allowContentAccess = true
                    allowFileAccess = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        pbLoadPage.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        pbLoadPage.visibility = View.GONE
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        if (url?.contains("static/favicon.ico") == true && youTubeVM.canAdShow && youTubeVM.adIsLoaded) {
                            youTubeVM.startAdTimer(TimeUnit.MINUTES.toMillis(1))
                            fragmentAdLayout.visibility = View.VISIBLE
                            webView.visibility = View.INVISIBLE
                        }
                    }
                }
                setOnScrollChangeListener(object : View.OnScrollChangeListener {
                    override fun onScrollChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int) {
                        return
                    }
                })


            }
            CookieManager.getInstance().apply {
                acceptCookie()
                setAcceptThirdPartyCookies(webView, true)
            }

            if (savedInstanceState == null) {
                webView.loadUrl(MOBILE_YOUTUBE_URL)
            }
            else {
                val bundle = savedInstanceState.getBundle(WEB_VIEW_BUNDLE)
                bundle?.let { webView.restoreState(it) }
            }

            youTubeVM.timerState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    YouTubeViewModel.TimerState.Finish -> {

                    }
                    YouTubeViewModel.TimerState.Start -> {
                        youTubeVM.adIsLoaded = false
                        parentFragmentManager.beginTransaction().replace(R.id.fragmentAdLayout, AdFragment.newInstance()).commit()
                    }
                    else -> {}
                }
            }

            setFragmentResultListener(KEY_AD_DISMISSED, listener = {requestKey, bundle ->
                fragmentAdLayout.visibility = View.INVISIBLE
                webView.visibility = View.VISIBLE
            })
            setFragmentResultListener(KEY_AD_LOADED, listener = {requestKey, bundle ->
                youTubeVM.adIsLoaded = true
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        with(binding!!) {
            val bundle = Bundle()
            webView.saveState(bundle)
            outState.putBundle(WEB_VIEW_BUNDLE, bundle)
        }
    }

    override fun onResume() {
        super.onResume()

        with(binding!!) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                    else {
                        parentFragmentManager.popBackStack()
                    }
                }
            })
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }


}