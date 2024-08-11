package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.myapp.lexicon.ads.interfaces.IAdDataListener
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.FragmentBannersBinding
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestError

class BannersFragment : Fragment(), IAdDataListener {

    companion object {
        val TAG = "${BannersFragment::class.java.simpleName}.TAG589758"
        const val ARG_ID_LIST = "BANNERS_IDS"
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

            val listBannerIds = arguments?.getStringArrayList(ARG_ID_LIST)?.toList()

            if (!listBannerIds.isNullOrEmpty()) {
                repeat(listBannerIds.size) {
                    val bannerView = BannerAdView(requireContext()).apply {
                        layoutParams = LinearLayoutCompat.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                    }
                    layoutBanners.addView(bannerView)
                }

                loadAndShowBanners(
                    this,
                    listBannerIds,
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
    }

    private fun loadAndShowBanners(
        binding: FragmentBannersBinding,
        idList: List<String>,
        onLoaded: () -> Unit,
        onImpression: (data: AdData?) -> Unit
    ) {
        with(binding) {

            val bannerAdIds = BannerAdIds.values().toList()

            var loadCount = 0
            val adData = AdData()
            val bannerViews = layoutBanners.children as Sequence<BannerAdView>
            bannerViews.forEachIndexed { index, adView ->
                adView.loadBanner(
                    adId = bannerAdIds.firstOrNull { it.id == idList[index] },
                    heightRate = 1.0 / idList.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            Throwable(it.description).printStackTraceIfDebug()
                        }
                        if (loadCount >= idList.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        if (data != null) {
                            adData.let {
                                it.adType = data.adType
                                it.adUnitId = data.adUnitId
                                it.blockId = data.blockId
                                it.currency = data.currency
                                it.network = data.network
                                it.precision = data.precision
                                it.requestId = data.requestId
                                it.revenue += data.revenue
                                it.revenueUSD += data.revenueUSD
                            }
                            if (loadCount >= idList.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
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