package com.myapp.lexicon.main;


import android.os.Bundle;
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
public class MainBannerFragment extends Fragment
{
    private AppData appData;
    private View view = null;

    public MainBannerFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        appData = AppData.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.a_fragment_main_banner, container, false);
        }

        if (savedInstanceState == null)
        {
            final AdView bannerView = (AdView) view.findViewById(R.id.adView_main);
            bannerView.setVisibility(View.GONE);
            MobileAds.initialize(getActivity(), getString(R.string.main_bottom_banner));
            AdRequest adRequest = new AdRequest.Builder().build();
            AdRequest adRequest1 = new AdRequest.Builder().addTestDevice("7162b61eda7337bb").build();
            bannerView.loadAd(adRequest1);
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
