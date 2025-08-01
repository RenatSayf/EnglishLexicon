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

                    listOf(adNative1, adNative2)
                        .showIfLoaded(
                            onNotLoaded = {

                            },
                            onShow = {
                                if (!isShown) {
                                    isShown = true
                                    lifecycleScope.launch {
                                        delay(5000)
                                        btnClose.visibility = View.VISIBLE
                                        btnClose.setOnClickListener {
                                            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
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
                        lifecycleScope.launch {
                            delay(500)
                            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
                        }
                    },
                    onClosed = {
                        lifecycleScope.launch {
                            delay(500)
                            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
                        }
                    }
                )
            }
            AdType.REWARDED.type -> {

                requireActivity().loadAndShowRewardedAd(
                    onNotLoaded = {
                        lifecycleScope.launch {
                            delay(500)
                            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
                        }
                    },
                    onClosed = {
                        lifecycleScope.launch {
                            delay(500)
                            parentFragmentManager.beginTransaction().remove(this@AdFragment).commit()
                        }
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

}