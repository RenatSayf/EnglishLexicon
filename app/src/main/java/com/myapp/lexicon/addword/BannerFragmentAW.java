package com.myapp.lexicon.addword;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.lexicon.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class BannerFragmentAW extends Fragment
{
    private View fragmentView = null;

    public BannerFragmentAW()
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
        if (fragmentView == null)
        {
            fragmentView = inflater.inflate(R.layout.b_banner_fragment_aw, container, false);
        }

//        if (savedInstanceState == null)
//        {
//            final AdView bannerView = fragmentView.findViewById(R.id.adView_aw);
//            bannerView.setVisibility(View.GONE);
//            MobileAds.initialize(getActivity(), getString(R.string.play_list_banner));
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
//        }

        return fragmentView;
    }

}
