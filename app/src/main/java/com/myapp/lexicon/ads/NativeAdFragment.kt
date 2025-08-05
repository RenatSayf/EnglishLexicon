package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.ads.ext.showNativeAdsIfLoaded
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.FragmentAdBinding
import com.myapp.lexicon.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NativeAdFragment : Fragment() {
    companion object {
        var isShown = false
        var onShown: () -> Unit = {}
        var onReward: (AdData) -> Unit = {}
        var onClosed: (coins: Int) -> Unit = {}

        fun newInstance(
            onShown: () -> Unit = {},
            onReward: (AdData) -> Unit = {},
            onClosed: (coins: Int) -> Unit = {}
        ): NativeAdFragment {
            this.onClosed = onClosed
            this.onReward = onReward
            this.onShown = onShown
            return NativeAdFragment()
        }
    }

    private var binding: FragmentAdBinding? = null

    private val adsVM: AdsViewModel by lazy {
        ViewModelProvider(requireActivity())[AdsViewModel::class]
    }

    private var coins: Int = 0

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
            adsVM.adReward.observe(viewLifecycleOwner) { value ->
                coins += value
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
                    }
                }
            )

        }
    }

    override fun onDestroyView() {

        isShown = false
        onClosed.invoke(coins)

        super.onDestroyView()
    }
}