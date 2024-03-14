package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.FragmentAdBinding
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.video.web.YouTubeFragment
import com.yandex.mobile.ads.interstitial.InterstitialAd

class AdFragment : Fragment() {

    companion object {
        fun newInstance() = AdFragment()
    }

    private var binding: FragmentAdBinding? = null

    private val adsVM: AdsViewModel by lazy {
        ViewModelProvider(this)[AdsViewModel::class.java]
    }
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_2)
        adsVM.interstitialAd.observe(this) { result ->
            result.onSuccess { ad: InterstitialAd ->
                interstitialAd = ad
                setFragmentResult(YouTubeFragment.KEY_AD_LOADED, Bundle.EMPTY)
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

    override fun onResume() {
        super.onResume()

        interstitialAd?.showAd(
            requireActivity(),
            onImpression = {data: AdData? ->
                "********** ${data.toString()} ***********".logIfDebug()
            },
            onDismissed = {
                setFragmentResult(YouTubeFragment.KEY_AD_DISMISSED, Bundle.EMPTY)
            }
        )
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

}