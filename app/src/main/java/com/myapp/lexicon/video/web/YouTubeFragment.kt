@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.video.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdFragment
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.ext.showAdPopup
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.PRETTY_PRINT_URL
import com.myapp.lexicon.common.VIDEO_URL
import com.myapp.lexicon.databinding.FragmentYouTubeBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.toDp
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.video.extensions.changeHeightAnimatedly
import com.myapp.lexicon.video.models.Bookmark.Companion.fromString
import com.myapp.lexicon.video.web.bookmarks.BookmarksDialog
import com.myapp.lexicon.video.web.models.UrlHistoryItem
import com.myapp.lexicon.video.web.pref.lastUrl
import kotlinx.serialization.json.Json


private const val WEB_VIEW_BUNDLE = "WEB_VIEW_BUNDLE"

class YouTubeFragment : Fragment() {
    companion object {

        const val KEY_AD_DISMISSED = "KEY_AD_DISMISSED_2548"
        const val KEY_AD_DATA = "KEY_AD_DATA_78541"
        const val KEY_JSON_AD_DATA = "KEY_JSON_AD_DATA_52398"

        @JvmStatic
        fun newInstance() = YouTubeFragment()
    }

    private var binding: FragmentYouTubeBinding? = null

    private val youTubeVM: YouTubeViewModel by lazy {
        ViewModelProvider(this)[YouTubeViewModel::class.java]
    }
    private val revenueVM: RevenueViewModel by lazy {
        ViewModelProvider(requireActivity())[RevenueViewModel::class.java]
    }
    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }
    private val actionBarHeight: Int by lazy {
        with(TypedValue().also {requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, it, true)}) {
            TypedValue.complexToDimensionPixelSize(this.data, resources.displayMetrics)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            youTubeVM.startAdTimer()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYouTubeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            includeNetError.layoutRoot.visibility = View.GONE

            webView.apply {
                canGoForward()
                canGoBack()
                settings.apply {
                    javaScriptEnabled = true
                    setSupportMultipleWindows(true)
                    javaScriptCanOpenWindowsAutomatically = true
                    allowContentAccess = true
                    allowFileAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        pbLoadPage.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        pbLoadPage.visibility = View.GONE
                        if (url != null && url.contains("watch?v=")) {
                            val videoUrl = webView.originalUrl
                            if (videoUrl != null) {
                                youTubeVM.urlList.add(UrlHistoryItem(System.currentTimeMillis(), videoUrl))
                            }
                        }
                        else if (url != null && url.contains("shorts")) {
                            val shortUrl = webView.url
                            if (shortUrl != null) {
                                youTubeVM.urlList.add(UrlHistoryItem(System.currentTimeMillis(), shortUrl))
                            }
                        }
                        else if (url != null && url == VIDEO_URL) {
                            webView.clearHistory()
                        }
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        if (url == PRETTY_PRINT_URL) {
                            val videoUrl = webView.originalUrl
                            val shortUrl = webView.url
                            if (videoUrl != null && videoUrl.contains("watch?v=")) {
                                youTubeVM.urlList.add(UrlHistoryItem(System.currentTimeMillis(), videoUrl))
                            }
                            else if (shortUrl != null && shortUrl.contains("/shorts/")) {
                                youTubeVM.urlList.add(UrlHistoryItem(System.currentTimeMillis(), shortUrl))
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (error?.errorCode == -2) {
                            youTubeVM.cancelTimer()
                            includeNetError.layoutRoot.visibility = View.VISIBLE
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                    }
                }

                addJavascriptInterface(youTubeVM, YouTubeViewModel.JS_TAG)
            }
            CookieManager.getInstance().apply {
                acceptCookie()
                setAcceptThirdPartyCookies(webView, true)
            }

            if (savedInstanceState == null) {
                val lastUrl = requireContext().lastUrl
                if (lastUrl == null) {
                    webView.loadUrl(VIDEO_URL)
                }
                else {
                    webView.loadUrl(lastUrl)
                }
            }
            else {
                val bundle = savedInstanceState.getBundle(WEB_VIEW_BUNDLE)
                bundle?.let { webView.restoreState(it) }
            }

            youTubeVM.timerState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    YouTubeViewModel.TimerState.Finish -> {

                        bottomBar.changeHeightAnimatedly(5.toDp)

                        vPopAnchor.showAdPopup(
                            onClick = {
                                webView.evaluateJavascript(
                                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                    object : ValueCallback<String> {
                                        override fun onReceiveValue(html: String?) {
                                            youTubeVM.parseIsPlayerPlay(
                                                rawHtml = html,
                                                onStart = {
                                                    locker.lock()
                                                },
                                                onComplete = { ex: Exception? ->
                                                    ex?.printStackTraceIfDebug()
                                                    locker.unLock()
                                                },
                                                onPlay = {
                                                    val url = youTubeVM.playPauseClickScript()
                                                    webView.loadUrl(url)
                                                    parentFragmentManager.beginTransaction().add(R.id.frame_to_page_fragm, AdFragment.newInstance()).commit()
                                                },
                                                onPause = {
                                                    parentFragmentManager.beginTransaction().add(R.id.frame_to_page_fragm, AdFragment.newInstance()).commit()
                                                }
                                            )
                                        }
                                    }
                                )
                            },
                            onDismissed = {
                                youTubeVM.startAdTimer()
                            }
                        )
                    }
                    else -> {}
                }
            }

            setFragmentResultListener(KEY_AD_DISMISSED, listener = {requestKey, bundle ->
                youTubeVM.startAdTimer()
                val url = youTubeVM.playPauseClickScript()
                webView.loadUrl(url)
            })

            if (savedInstanceState == null) {
                revenueVM.getUserFromCloud()
            }

            //the touch listener is used because the scroll listener stops working after a while
            webView.setOnTouchListener(object : View.OnTouchListener {

                private var downTouchY: Float = -1.0f
                private var moveTouchY: Float = -1.0f

                override fun onTouch(view: View?, event: MotionEvent?): Boolean {

                    val action = event?.action
                    when(action) {
                        MotionEvent.ACTION_DOWN -> {
                            downTouchY = event.y
                        }
                        MotionEvent.ACTION_MOVE -> {
                            moveTouchY = event.y
                        }
                        MotionEvent.ACTION_UP -> {
                            moveTouchY = event.y
                        }
                    }
                    if (action == MotionEvent.ACTION_MOVE && downTouchY > moveTouchY) {
                        bottomBar.changeHeightAnimatedly(
                            actionBarHeight,
                            onEnd = { isVisible: Boolean ->
                                if (!isVisible) {
                                    if (revenueVM.state.value is UserViewModel.State.RevenueUpdated) {
                                        val user = (revenueVM.state.value!! as UserViewModel.State.RevenueUpdated).user
                                        revenueVM.setState(UserViewModel.State.ReceivedUserData(user))
                                    }
                                }
                            }
                        )
                    }
                    else if (action == MotionEvent.ACTION_MOVE && downTouchY < moveTouchY) {
                        bottomBar.changeHeightAnimatedly(5.toDp)
                    }
                    return false
                }
            })

            btnBookmarks.setOnClickListener {
                BookmarksDialog.newInstance().show(parentFragmentManager, BookmarksDialog.TAG)
            }

            btnTest.setOnClickListener {

            }

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

    override fun onPause() {

        val lastUrl = youTubeVM.getLastLoadedUrl()
        if (lastUrl.isNotEmpty()) {
            requireContext().lastUrl = lastUrl
        }

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        with(binding!!) {

            setFragmentResultListener(KEY_AD_DATA, listener = {requestKey: String, bundle: Bundle ->
                val strData = bundle.getString(KEY_JSON_AD_DATA)
                if (strData != null) {
                    val adData = try {
                        Json.decodeFromString<AdData>(strData)
                    } catch (e: Exception) {
                        null
                    }
                    adData?.let { data: AdData ->
                        revenueVM.updateUserRevenueIntoCloud(data)
                    }
                }
            })
            setFragmentResultListener(BookmarksDialog.KEY_BOOKMARK_RESULT, listener = {requestKey: String, bundle: Bundle ->
                val bookmark = bundle.getString(BookmarksDialog.KEY_SELECTED_BOOKMARK)?.fromString()
                bookmark?.let {
                    webView.loadUrl(it.url)
                }
            })

            revenueVM.userRevenueLD.observe(viewLifecycleOwner) { result ->
                result.onError { throwable ->
                    throwable.printStackTraceIfDebug()
                }
            }

            revenueVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is UserViewModel.State.ReceivedUserData -> {
                        val rewardText = "${getString(R.string.coins_bag)} " +
                                "${getString(R.string.text_your_reward)} ${state.user.userReward.to2DigitsScale()} ${state.user.currencySymbol}"
                        tvReward.text = rewardText
                    }
                    is UserViewModel.State.RevenueUpdated -> {
                        val rewardText = "${getString(R.string.coins_bag)}  +${state.bonus.to2DigitsScale()} ${state.user.currencySymbol}. " +
                                "${getString(R.string.text_your_reward)} ${state.user.userReward.to2DigitsScale()} ${state.user.currencySymbol}"
                        tvReward.text = rewardText
                        bottomBar.changeHeightAnimatedly(actionBarHeight)
                    }
                    else -> {}
                }
            }

            btnBack.setOnClickListener {
                goBack()
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBack()
                }
            })
        }
    }

    private fun FragmentYouTubeBinding.goBack() {
        if (webView.canGoBack()) {
            webView.loadUrl(VIDEO_URL)
        }
        else if (!webView.canGoBack() && webView.url?.contains("shorts") == true) {
            webView.loadUrl(VIDEO_URL)
        }
        else {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }


}