package com.myapp.lexicon.main;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;

import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.billing.DonateViewModel;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.JavaKotlinMediator;
import com.myapp.lexicon.models.PurchaseToken;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.interstitial.InterstitialAd;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;


public class BackgroundFragm extends Fragment
{
    private View fragmentView = null;
    public InterstitialAd yandexAd = null;

    // картинки для фона
    public static int[] imagesId = new int[]
            {
                    R.drawable.img_uk,
                    R.drawable.img_uk1,
                    R.drawable.img_uk2,
                    R.drawable.img_uk3,
                    R.drawable.img_uk4,
                    R.drawable.img_uk5,
                    R.drawable.img_uk6,
                    R.drawable.img_uk7,
                    R.drawable.img_uk8,
                    R.drawable.img_uk9,
                    R.drawable.img_uk10,
                    R.drawable.img_uk11,
                    R.drawable.img_uk12,
                    R.drawable.img_uk13,
                    R.drawable.img_uk14,
                    R.drawable.img_uk15,
                    R.drawable.img_uk16,
                    R.drawable.img_uk17,
                    R.drawable.img_uk18,
                    R.drawable.img_uk19,
                    R.drawable.img_uk20,
                    R.drawable.img_uk21,
                    R.drawable.img_uk22,
                    R.drawable.img_uk23,
                    R.drawable.img_uk24,
                    R.drawable.img_uk25,
                    R.drawable.img_uk26,
                    R.drawable.img_uk27,
                    R.drawable.img_uk28,
                    R.drawable.img_usa3,
                    R.drawable.img_usa4
            };

    public BackgroundFragm()
    {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void loadAds()
    {
        JavaKotlinMediator mediator = new JavaKotlinMediator();
        mediator.loadInterstitialAd(requireContext(), 3, new JavaKotlinMediator.InterstitialAdListener()
        {
            @Override
            public void onSuccess(@NonNull InterstitialAd ad)
            {
                if (BuildConfig.DEBUG)
                {
                    System.out.println("************* InterstitialAd is loaded ******************");
                }
                yandexAd = ad;
            }

            @Override
            public void onError(@NonNull AdRequestError error)
            {
                if (BuildConfig.DEBUG) {
                    System.out.println("**************** InterstitialAd Error: " + error.getDescription() + " *******************");
                }
            }
        });

        BannerAdView adBanner = requireActivity().findViewById(R.id.banner_main);
        if (adBanner != null)
        {
            mediator = new JavaKotlinMediator();
            mediator.loadBannerAd(requireContext(), 0, adBanner, new JavaKotlinMediator.BannerAdListener()
            {
                @Override
                public void onSuccess()
                {
                    if (BuildConfig.DEBUG)
                    {
                        System.out.println("************* Banner is loaded ******************");
                    }
                }

                @Override
                public void onError(@NonNull AdRequestError error)
                {
                    if (BuildConfig.DEBUG)
                    {
                        System.out.println("**************** Banner Error: " + error.getDescription() + " *******************");
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (fragmentView == null)   // TODO: Fragment 4. Обязательная проверка, что бы не раздувать новый макет при повороте экрана
        {
            fragmentView = inflater.inflate(R.layout.a_fragment_background, container, false);
        }

        if (savedInstanceState == null) // проверка, что бы не перезапускался AdapterViewFlipper каждый раз при повороте экрана
        {
            // TODO: AdapterViewFlipper: 1. Этот компонент будет использоваться для анимированного изменения фонового изображения
            AdapterViewFlipper adapterViewFlipper = fragmentView.findViewById(R.id.adapter_view_flipper);

            // TODO: AdapterViewFlipper: 7. создание адаптера и запуск анимации
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                FlipperAdapter flipperAdapter = new FlipperAdapter(activity, imagesId);
                adapterViewFlipper.setAdapter(flipperAdapter);
                adapterViewFlipper.setFlipInterval(20000);

                // предварительно создать в директории res новую директорию animator и добавить в нее ресурсы анимации (см. директорию res/animator)
                adapterViewFlipper.setInAnimation(getActivity(), R.animator.in_animator);
                adapterViewFlipper.setOutAnimation(getActivity(), R.animator.out_animator);
                adapterViewFlipper.setAutoStart(true);
            }
        }

        return fragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        DonateViewModel donateVM = new ViewModelProvider(this).get(DonateViewModel.class);

        ExtensionsKt.checkAdsToken(this, () -> {

            donateVM.getNoAdsToken().observe(this, token -> {
                if (token == PurchaseToken.YES) {
                    ExtensionsKt.saveNoAdsToken(this, "XXXXXXXXXXXXXXXXXXX");
                }
                else if (token == PurchaseToken.NO) {
                    ExtensionsKt.saveNoAdsToken(this, "");
                    loadAds();
                }
            });
            return null;
        }, () -> {
            loadAds();
            return null;
        }, () -> null);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (yandexAd != null)
                {
                    new JavaKotlinMediator().showInterstitialAd(yandexAd, () -> requireActivity().finish());
                } else
                {
                    requireActivity().finish();
                }
            }
        });
    }

}
