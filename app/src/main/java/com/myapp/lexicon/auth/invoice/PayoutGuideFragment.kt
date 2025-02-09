package com.myapp.lexicon.auth.invoice

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.myapp.lexicon.R
import com.myapp.lexicon.common.SELF_EMPLOYED_MARKET
import com.myapp.lexicon.common.SELF_EMPLOYED_PACKAGE
import com.myapp.lexicon.common.SELF_EMPLOYED_RU_STORE
import com.myapp.lexicon.databinding.FragmentPayoutGuideBinding
import com.myapp.lexicon.models.User

class PayoutGuideFragment : Fragment() {

    companion object {

        private var user: User? = null

        fun newInstance(user: User): PayoutGuideFragment {
            this.user = user
            return PayoutGuideFragment()
        }
    }

    private var binding: FragmentPayoutGuideBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayoutGuideBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            toolBar.apply {
                subtitle = "${getString(R.string.text_your_reward)} ${user?.reservedPayment?.toInt()} ${user?.currencySymbol}"
            }

            webView.apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.loadUrl("javascript:(function(){ document.body.style.marginBottom = '64px'})();")
                        progressBar.visibility = View.GONE
                    }
                }
                loadUrl("https://api.englishlexicon.ru/get-self-employed-guide")
            }

            btnOpenApp.setOnClickListener {
                val intent = requireContext().packageManager.getLaunchIntentForPackage(SELF_EMPLOYED_PACKAGE)
                if (intent != null) {
                    startActivity(intent.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, SELF_EMPLOYED_MARKET))
                    } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW, SELF_EMPLOYED_RU_STORE))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        with(binding!!) {
            toolBar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            })
        }
    }

    override fun onDestroy() {

        binding = null

        super.onDestroy()
    }
}