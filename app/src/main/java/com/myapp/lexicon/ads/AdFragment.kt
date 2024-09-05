@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.common.AD_TYPE
import com.myapp.lexicon.databinding.FragmentAdBinding
import com.myapp.lexicon.video.web.YouTubeFragment
import com.yandex.mobile.ads.interstitial.InterstitialAd
import kotlinx.serialization.json.Json

class AdFragment : Fragment() {

    companion object {
        fun newInstance() = AdFragment()
    }

    private var binding: FragmentAdBinding? = null

    private val adsVM: AdsViewModel by lazy {
        ViewModelProvider(this)[AdsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when(AD_TYPE) {
            AdType.BANNER -> {
                requireActivity().startBannersActivity(
                    onImpression = {data: AdData? ->
                        setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                            if (data != null) {
                                val jsonData = Json.encodeToJsonElement(AdData.serializer(), data).toString()
                                putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                            }
                        })
                    },
                    onDismissed = {
                        setFragmentResult(YouTubeFragment.KEY_AD_DISMISSED, Bundle.EMPTY)
                    }
                )
            }
            AdType.NATIVE -> {
                requireActivity().startNativeAdsActivity(
                    onImpression = {data: AdData? ->
                        setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                            if (data != null) {
                                val jsonData = Json.encodeToJsonElement(AdData.serializer(), data).toString()
                                putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                            }
                        })
                    },
                    onDismissed = {bonus: Double ->
                        setFragmentResult(YouTubeFragment.KEY_AD_DISMISSED, Bundle.EMPTY)
                    }
                )
            }
            AdType.INTERSTITIAL -> {
                adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_2)
                adsVM.interstitialAd.observe(this) { result ->
                    result.onSuccess { ad: InterstitialAd ->
                        ad.showAd(
                            requireActivity(),
                            onImpression = {data: AdData? ->
                                setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                                    if (data != null) {
                                        val jsonData = Json.encodeToJsonElement(AdData.serializer(), data).toString()
                                        putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                                    }
                                })
                            },
                            onDismissed = {
                                setFragmentResult(YouTubeFragment.KEY_AD_DISMISSED, Bundle.EMPTY)
                            }
                        )
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

}