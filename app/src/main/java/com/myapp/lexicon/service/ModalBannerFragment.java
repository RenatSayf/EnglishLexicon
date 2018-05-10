package com.myapp.lexicon.service;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModalBannerFragment extends Fragment
{
    private View view = null;

    public ModalBannerFragment()
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
        if (view == null)
        {
            view = inflater.inflate(R.layout.a_fragment_main_banner, container, false);
        }

        if (savedInstanceState == null)
        {
            final AdView bannerView = view.findViewById(R.id.adView_main);
            bannerView.setVisibility(View.GONE);
            MobileAds.initialize(getActivity(), getString(R.string.modal_bottom_banner));
            AdRequest adRequest;
            if (AppData.getInstance().testDeviceEnabled())
            {
                adRequest = new AdRequest.Builder().addTestDevice(AppData.getInstance().getTestDeviceID()).build();
            } else
            {
                adRequest = new AdRequest.Builder().build();
            }
            bannerView.loadAd(adRequest);
            bannerView.setAdListener(new AdListener()
            {
                @Override
                public void onAdLoaded()
                {
                    super.onAdLoaded();
                    bannerView.setVisibility(View.VISIBLE);
                }
            });
        }

        return view;
    }

}
