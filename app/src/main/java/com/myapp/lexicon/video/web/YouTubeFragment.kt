@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.video.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
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
import com.myapp.lexicon.common.MOBILE_YOUTUBE_URL
import com.myapp.lexicon.databinding.FragmentYouTubeBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.toDp
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.video.extensions.changeHeightAnimatedly
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
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        pbLoadPage.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        pbLoadPage.visibility = View.GONE
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {}
                }

                setOnScrollChangeListener(object : View.OnScrollChangeListener {
                    override fun onScrollChange(p0: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                        when {
                            oldScrollY > scrollY -> {
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
                            else -> {
                                if (oldScrollY != scrollY) {
                                    bottomBar.changeHeightAnimatedly(5.toDp)
                                }
                            }
                        }
                    }
                })
                addJavascriptInterface(youTubeVM, YouTubeViewModel.JS_TAG)
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
                                                    val url = youTubeVM.performClickScript()
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
                val url = youTubeVM.performClickScript()
                webView.loadUrl(url)
            })

            if (savedInstanceState == null) {
                revenueVM.getUserFromCloud()
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

            revenueVM.userRevenueLD.observe(viewLifecycleOwner) { result ->
                result.onError { throwable ->
                    throwable.printStackTraceIfDebug()
                }
            }

            revenueVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is UserViewModel.State.ReceivedUserData -> {
                        val rewardText = "${getString(R.string.coins_bag)} " +
                                "${getString(R.string.text_your_reward)} ${state.user.userReward} ${state.user.currencySymbol}"
                        tvReward.text = rewardText
                    }
                    is UserViewModel.State.RevenueUpdated -> {
                        val rewardText = "${getString(R.string.coins_bag)}  +${state.bonus} ${state.user.currencySymbol}. " +
                                "${getString(R.string.text_your_reward)} ${state.user.userReward} ${state.user.currencySymbol}"
                        tvReward.text = rewardText
                        bottomBar.changeHeightAnimatedly(actionBarHeight)
                    }
                    else -> {}
                }
            }

            btnBack.setOnClickListener {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
                else {
                    parentFragmentManager.popBackStack()
                }
            }

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