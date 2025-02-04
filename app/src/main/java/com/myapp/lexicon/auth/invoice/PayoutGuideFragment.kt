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
import com.myapp.lexicon.common.SELF_EMPLOYED_MARKET
import com.myapp.lexicon.common.SELF_EMPLOYED_PACKAGE
import com.myapp.lexicon.common.SELF_EMPLOYED_RU_STORE
import com.myapp.lexicon.databinding.FragmentPayoutGuideBinding

class PayoutGuideFragment : Fragment() {

    companion object {

        private var amount: Int = 0

        fun newInstance(amount: Int): PayoutGuideFragment {
            this.amount = amount
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

            webView.apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                    }

                    override fun onPageFinished(view: WebView?, url: String?) {

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