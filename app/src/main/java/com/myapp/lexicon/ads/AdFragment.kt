@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.ads.ext.loadAndShowRewardedAd
import com.myapp.lexicon.ads.ext.showIfLoaded
import com.myapp.lexicon.ads.ext.showInterstitialIfLoaded
import com.myapp.lexicon.ads.models.AD_VIDEO
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.databinding.FragmentAdBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdFragment : Fragment() {

    companion object {
        var isShown = false

        fun newInstance(onCreate: () -> Unit): AdFragment {
            onCreate.invoke()
            return AdFragment()
        }
    }

    private var binding: FragmentAdBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(AD_VIDEO) {

            AdType.NATIVE.type -> {

                with(binding!!) {

                    var failureLoadCount = 0
                    listOf(adNative1, adNative2)
                        .showIfLoaded(
                            onNotLoaded = {
                                failureLoadCount++
                                if (failureLoadCount >= 2) {
                                    removeThisFragmentWithDelay(100)
                                }
                            },
                            onShow = {
                                if (!isShown) {
                                    isShown = true
                                    lifecycleScope.launch {
                                        delay(5000)
                                        btnClose.visibility = View.VISIBLE
                                        btnClose.setOnClickListener {
                                            removeThisFragmentWithDelay(100)
                                        }
                                    }
                                }
                            }
                        )
                }
            }
            AdType.INTERSTITIAL.type -> {

                requireActivity().showInterstitialIfLoaded(
                    onNotLoaded = {
                        removeThisFragmentWithDelay()
                    },
                    onClosed = {
                        removeThisFragmentWithDelay()
                    }
                )
            }
            AdType.REWARDED.type -> {

                requireActivity().loadAndShowRewardedAd(
                    onNotLoaded = {
                        removeThisFragmentWithDelay()
                    },
                    onClosed = {
                        removeThisFragmentWithDelay()
                    }
                )
            }
        }
    }

    override fun onDestroy() {

        binding = null
        isShown = false

        super.onDestroy()
    }

    fun removeThisFragmentWithDelay(timeInMillis: Long = 500) {
        lifecycleScope.launch {
            delay(timeInMillis)
            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
        }
    }

}