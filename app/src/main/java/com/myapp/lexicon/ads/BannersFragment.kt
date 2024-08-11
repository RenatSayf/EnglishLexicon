package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.myapp.lexicon.ads.interfaces.IAdDataListener
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.FragmentBannersBinding
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.yandex.mobile.ads.common.AdRequestError

class BannersFragment : Fragment(), IAdDataListener {

    companion object {
        val TAG = "${BannersFragment::class.java.simpleName}.TAG589758"
    }

    private var binding: FragmentBannersBinding? = null

    private val revenueVM by activityViewModels<RevenueViewModel>()

    private var listener: IAdDataListener? = null

    fun setAdDataListener(listener: IAdDataListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBannersBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            pbLoadAds.visibility = View.VISIBLE

            loadAndShowBanners(
                this,
                onLoaded = {
                    pbLoadAds.visibility = View.GONE
                },
                onImpression = {data: AdData? ->
                    if (data != null) {
                        listener?.onImpression(data)
                    } else {
                        listener?.onImpression(null)
                    }
                }
            )
        }
    }

    private fun loadAndShowBanners(
        binding: FragmentBannersBinding,
        onLoaded: () -> Unit,
        onImpression: (data: AdData?) -> Unit
    ) {
        with(binding) {

            var loadCount = 0
            val listBannerIds = listOf(BannerAdIds.BANNER_3, BannerAdIds.BANNER_4, BannerAdIds.BANNER_5)
            val bannerViews = listOf(bannerView1, bannerView2, bannerView3)
            bannerViews.forEachIndexed { index, adView ->
                adView.loadBanner(
                    adId = listBannerIds[index],
                    heightRate = 0.33,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            Throwable(it.description).printStackTraceIfDebug()
                        }
                        if (loadCount >= 3) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = {data: AdData? ->
                        if (data != null) {
                            onImpression.invoke(data)
                        }
                        else onImpression.invoke(null)
                    }
                )
            }
        }
    }

    override fun onImpression(data: AdData?) {
        if (data != null) {
            revenueVM.updateUserRevenueIntoCloud(data)
        }
    }
}