package com.myapp.lexicon.main;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsExtensionsKt;
import com.myapp.lexicon.cloudstorage.DownloadDbWorker;
import com.myapp.lexicon.cloudstorage.StorageDialog;
import com.myapp.lexicon.cloudstorage.UploadDbWorker;
import com.myapp.lexicon.databinding.DialogStorageBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.JavaKotlinMediator;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.interstitial.InterstitialAd;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


@SuppressWarnings("Convert2Lambda")
public class BackgroundFragm extends Fragment
{
    private View fragmentView = null;
    public InterstitialAd yandexAd = null;

    // картинки для фона
    private final int[] imagesId = new int[]
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

    private void hideAdBanner() {
        BannerAdView adBanner = requireActivity().findViewById(R.id.banner_main);
        if (adBanner != null) {
            adBanner.setVisibility(View.GONE);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        String dbName = getString(R.string.data_base_name);
        if (SettingsExtKt.getCheckFirstLaunch(requireContext()))
        {
            AdsExtensionsKt.getAdvertisingID(requireContext(), id -> {

                DownloadDbWorker.Companion.downloadDbFromCloud(requireContext(), dbName, id, new DownloadDbWorker.Listener()
                {
                    @Override
                    public void onSuccess(@NonNull byte[] bytes)
                    {
                        String[] databaseList = requireContext().databaseList();
                        List<String> list = Arrays.stream(databaseList).filter(Predicate.isEqual(dbName)).collect(Collectors.toList());
                        if (!list.isEmpty()) {
                            File databaseFile = requireContext().getDatabasePath(list.get(0));
                            byte[] currentBytes = ExtensionsKt.fileToBytes(BackgroundFragm.this, databaseFile);
                            boolean isEquals = Arrays.equals(currentBytes, bytes);
                            if (!isEquals) {
                                showCloudDialog(list.get(0), bytes);
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull String error)
                    {
                        if (BuildConfig.DEBUG) {
                            new Exception(error).printStackTrace();
                        }
                    }
                    @Override
                    public void onComplete()
                    {
                        SettingsExtKt.setCheckFirstLaunch(requireContext(), false);
                    }
                });
                return null;
            }, () -> null, err -> null, () -> null);
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();

        boolean adsIsEnabled = SettingsExtKt.getAdsIsEnabled(this);
        if (adsIsEnabled) {
            loadAds();
        }
        else {
            hideAdBanner();
            yandexAd = null;
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                ExtensionsKt.alarmClockEnable(requireContext());

                boolean storageEnabled = SettingsExtKt.getCloudStorageEnabled(requireContext());
                if (storageEnabled) {
                    AdsExtensionsKt.getAdvertisingID(requireContext(), id -> {
                                UploadDbWorker.Companion.uploadDbToCloud(
                                        requireContext(),
                                        getString(R.string.data_base_name),
                                        id,
                                        null
                                );
                                return null;
                            }, () -> null,
                            error -> null,
                            () -> null);
                }

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

    private void showCloudDialog(String file, byte[] bytes) {
        BottomSheetDialogFragment dialog = (BottomSheetDialogFragment)getParentFragmentManager().findFragmentByTag(StorageDialog.Companion.getTAG());
        if (dialog == null) {
            dialog = StorageDialog.Companion.newInstance(new StorageDialog.Listener()
            {
                @Override
                public void onDestroy()
                {
                    new LockOrientation(requireActivity()).unLock();
                }

                @Override
                public void onLaunch(@NonNull DialogStorageBinding binding)
                {
                    new LockOrientation(requireActivity()).lock();
                    binding.tvProductName.setText(getString(R.string.text_cloud_storage));
                    binding.tvPriceTitle.setText("В облачном хранилище найдена резервная копия Ваших словарей.\nВосстановить слова?");
                    binding.btnCloudEnable.setText("Восстановить");
                    binding.btnCancel.setText(getString(R.string.btn_text_cancel));
                }

                @Override
                public void onPositiveClick()
                {
                    try
                    {
                        FileOutputStream outputStream = requireContext().openFileOutput(file, Context.MODE_PRIVATE);
                        outputStream.write(bytes);
                        outputStream.close();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelClick()
                {}
            });
            dialog.show(getParentFragmentManager(), StorageDialog.Companion.getTAG());
        }
    }

}
