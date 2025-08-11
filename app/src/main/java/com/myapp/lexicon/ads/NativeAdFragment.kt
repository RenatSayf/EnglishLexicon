package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.appodeal.ads.Appodeal
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.ext.createTestRevenueInfo
import com.myapp.lexicon.ads.ext.revenueUpdateListener
import com.myapp.lexicon.ads.ext.showNativeAdsIfLoaded
import com.myapp.lexicon.ads.ext.updateRevenueOnCloud
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.databinding.FragmentAdBinding
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NativeAdFragment : Fragment() {
    companion object {
        var isShown = false
        var onShown: () -> Unit = {}
        var onReward: (AdData) -> Unit = {}
        var onClosed: (coins: Int, user: User) -> Unit = { coins, user ->

        }

        var onRevenueEmpty: () -> Unit = {}

        fun newInstance(
            onShown: () -> Unit = {},
            onReward: (AdData) -> Unit = {},
            onClosed: (coins: Int, user: User) -> Unit,
            onRevenueEmpty: () -> Unit
        ): NativeAdFragment {
            this.onClosed = onClosed
            this.onReward = onReward
            this.onShown = onShown
            this.onRevenueEmpty = onRevenueEmpty
            return NativeAdFragment()
        }
    }

    private var binding: FragmentAdBinding? = null

    private var coins: Int = 0
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onShown.invoke()

        with(binding!!) {

            coins = 0

            requireActivity().revenueUpdateListener { coins, user ->
                this@NativeAdFragment.coins += coins
                this@NativeAdFragment.user = user
            }

            var failureLoadCount = 0

            val ads = listOf(adNative1, adNative2)

            (requireActivity() as MainActivity).showNativeAdsIfLoaded(
                adsList = ads,
                onNotAvailableAds = {
                    parentFragmentManager.beginTransaction().remove(this@NativeAdFragment).commit()
                },
                onNotLoaded = {
                    failureLoadCount++
                    if (failureLoadCount >= ads.size) {
                        parentFragmentManager.beginTransaction().remove(this@NativeAdFragment).commit()
                    }
                },
                onShow = {
                    if (!isShown) {
                        isShown = true
                        lifecycleScope.launch {
                            delay(5000)
                            btnClose.visibility = View.VISIBLE
                            btnClose.setOnClickListener {
                                parentFragmentManager.beginTransaction().remove(this@NativeAdFragment).commit()
                            }
                        }
                        if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                            val testRevenueInfo = createTestRevenueInfo(2, "Native")
                            requireActivity().updateRevenueOnCloud(testRevenueInfo) { coins, user ->
                                this@NativeAdFragment.coins += coins
                                this@NativeAdFragment.user = user
                            }
                        }
                    }
                }
            )

        }
    }

    override fun onDestroyView() {

        isShown = false
        Appodeal.setAdRevenueCallbacks(null)
        user?.let { user ->
            if (coins > 0) {
                onClosed.invoke(coins, user)
            }
        }?: run {
            onRevenueEmpty.invoke()
        }

        super.onDestroyView()
    }
}