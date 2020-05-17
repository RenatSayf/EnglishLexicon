package com.myapp.lexicon.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



public class BannerFragmentPL extends Fragment
{
    private View fragment_view = null;

    public BannerFragmentPL()
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
            fragment_view = inflater.inflate(R.layout.p_banner_fragment_pl,container, false);
        }

        if (savedInstanceState == null)
        {
            final AdView bannerView = fragment_view.findViewById(R.id.adView_pl);
            bannerView.setVisibility(View.GONE);
            MobileAds.initialize(getActivity(), getString(R.string.play_list_banner));
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

        return fragment_view;
    }

}
