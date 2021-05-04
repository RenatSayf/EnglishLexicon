package com.myapp.lexicon.wordeditor;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.lexicon.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class BottomBannerFragmentWE extends Fragment
{
    private View fragment_view = null;
    public static final String TAG = "banner_fragment_we";

    public BottomBannerFragmentWE()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (fragment_view == null)
        {
            fragment_view = inflater.inflate(R.layout.d_bottom_banner_fragm_we, container, false);
        }

        if (savedInstanceState == null)
        {
//            final AdView bannerView = fragment_view.findViewById(R.id.adView_we);
//            bannerView.setVisibility(View.GONE);
//            MobileAds.initialize(getActivity(), getString(R.string.main_bottom_banner));
//            AdRequest adRequest;
//            if (AppData.getInstance().testDeviceEnabled())
//            {
//                adRequest = new AdRequest.Builder().addTestDevice(AppData.getInstance().getTestDeviceID()).build();
//            } else
//            {
//                adRequest = new AdRequest.Builder().build();
//            }
//            bannerView.loadAd(adRequest);
//            bannerView.setAdListener(new AdListener()
//            {
//                @Override
//                public void onAdLoaded()
//                {
//                    super.onAdLoaded();
//                    bannerView.setVisibility(View.VISIBLE);
//                }
//            });
        }

        return fragment_view;
    }

}
