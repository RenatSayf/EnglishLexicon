@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.myapp.lexicon.ads.feed_ad.startFeedAdsActivity
import com.myapp.lexicon.ads.interstitial.loadInterstitialAd
import com.myapp.lexicon.ads.interstitial.showInterstitialAd
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.ads.native_ad.startNativeAdsActivity
import com.myapp.lexicon.ads.rewarded.loadRewardedAd
import com.myapp.lexicon.ads.rewarded.showRewardedAd
import com.myapp.lexicon.databinding.FragmentAdBinding
import com.myapp.lexicon.di.App
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.ext.redirectToAuthScreen
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.video.web.YouTubeFragment
import kotlinx.serialization.json.Json

class AdFragment : Fragment() {

    companion object {
        fun newInstance() = AdFragment()
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

        val config = App.INSTANCE.defaultConfig
        when(config.adTypePerScreen.video) {
            AdType.NATIVE.type -> {
                requireActivity().startNativeAdsActivity(
                    adId = config.nativeIds.video,
                    onDismissed = { reward ->
                        setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                            val jsonData = Json.encodeToJsonElement(AdsReward.serializer(), reward).toString()
                            putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                        })
                        parentFragmentManager.beginTransaction().remove(this).commit()
                    },
                    onError = { error: String ->
                        Exception(error).printStackTraceIfDebug()
                        parentFragmentManager.popBackStack()
                    }
                )
            }
            AdType.INTERSTITIAL.type -> {
                requireActivity().loadInterstitialAd(
                    adId = config.interstitialAdIds.video,
                    onLoaded = { ad ->
                        requireActivity().showInterstitialAd(
                            onDismissed = { reward ->
                                setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                                    val jsonData = Json.encodeToJsonElement(AdsReward.serializer(), reward).toString()
                                    putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                                })
                            },
                            onAuthorizationRequired = {
                                parentFragmentManager.beginTransaction().remove(this).commit()
                                requireActivity().redirectToAuthScreen()
                            }
                        )
                    }
                )
            }
            AdType.REWARDED.type -> {
                requireActivity().loadRewardedAd(
                    adId = config.rewardedIds.video,
                    onLoaded = {
                        requireActivity().showRewardedAd(
                            onDismissed = { reward ->
                                setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                                    val jsonData = Json.encodeToJsonElement(AdsReward.serializer(), reward).toString()
                                    putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                                })
                            },
                            onAuthorizationRequired = {
                                parentFragmentManager.beginTransaction().remove(this).commit()
                                requireActivity().redirectToAuthScreen()
                            }
                        )
                    }
                )
            }
            AdType.FEED.type -> {
                requireActivity().startFeedAdsActivity(
                    adId = config.feedIds.video,
                    onDismissed = { reward ->
                        setFragmentResult(YouTubeFragment.KEY_AD_DATA, Bundle().apply {
                            val jsonData = Json.encodeToJsonElement(AdsReward.serializer(), reward).toString()
                            putString(YouTubeFragment.KEY_JSON_AD_DATA, jsonData)
                        })
                        parentFragmentManager.beginTransaction().remove(this).commit()
                    },
                    onError = { error ->
                        Exception(error).printStackTraceIfDebug()
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

}