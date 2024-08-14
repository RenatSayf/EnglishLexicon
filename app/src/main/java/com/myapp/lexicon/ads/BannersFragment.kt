package com.myapp.lexicon.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.FragmentBannersBinding
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BannersFragment : Fragment() {

    companion object {
        val TAG = "${BannersFragment::class.java.simpleName}.TAG589758"
        const val ARG_ID_LIST = "BANNERS_IDS"
    }

    private var binding: FragmentBannersBinding? = null

    interface Listener {
        fun onDismissed(data: AdData?)
    }

    private var listener: Listener? = null

    private var adData: AdData? = null

    fun setAdDataListener(listener: Listener) {
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

                val bannerAdIds = BannerAdIds.entries.filter {
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
                        this@BannersFragment.adData = data
                        lifecycleScope.launch {
                            delay(5000)
                            btnClose.visibility = View.VISIBLE
                        }
                    }
                )
            }

            btnClose.setOnClickListener {
                listener?.onDismissed(this@BannersFragment.adData)
                parentFragmentManager.popBackStack()
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

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }
}

fun FragmentManager.runBannerFragment(
    onImpression: (data: AdData?) -> Unit,
    onDismissed: (bonus: Double) -> Unit
) {
    val bannersFragment = BannersFragment().apply {
        arguments = Bundle().apply {
            putStringArrayList(
                BannersFragment.ARG_ID_LIST,
                arrayListOf(
                    BannerAdIds.BANNER_3.id,
                    BannerAdIds.BANNER_4.id
                )
            )
        }
        setAdDataListener(object : BannersFragment.Listener {
            override fun onDismissed(data: AdData?) {
                if (data != null) {
                    onImpression.invoke(data)
                    val bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                    onDismissed.invoke(bonus)
                }
            }
        })
    }
    this.beginTransaction()
        .add(R.id.frame_to_page_fragm, bannersFragment)
        .addToBackStack(null)
        .commit()
}