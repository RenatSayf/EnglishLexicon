package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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

                val bannerAdIds = BannerAdIds.values().filter {
                    listBannerIds.contains(it.id)
                }
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
                    bannerAdIds,
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

            btnClose.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .remove(this@BannersFragment)
                    .commit()
            }
        }
    }

    private fun loadAndShowBanners(
        binding: FragmentBannersBinding,
        idList: List<BannerAdIds>,
        onLoaded: () -> Unit,
        onImpression: (data: AdData?) -> Unit
    ) {
        with(binding) {

            val adIds = idList.take(3)
            var loadCount = 0
            var impressionCount = 0
            val adData = AdData()
            val banners = layoutBanners.children.toList()
            try {
                (banners[0] as BannerAdView).loadBanner(
                    adId = adIds[0],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            Throwable(it.description).printStackTraceIfDebug()
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
            }

            try {
                (banners[1] as BannerAdView).loadBanner(
                    adId = adIds[1],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            Throwable(it.description).printStackTraceIfDebug()
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
            }

            try {
                (banners[2] as BannerAdView).loadBanner(
                    adId = adIds[2],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            Throwable(it.description).printStackTraceIfDebug()
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
            }

        }
    }

    override fun onImpression(data: AdData?) {
        if (data != null) {
            binding?.btnClose?.visibility = View.VISIBLE
            revenueVM.updateUserRevenueIntoCloud(data)
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }
}