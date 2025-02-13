package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsViewModel;
import com.myapp.lexicon.ads.AdsViewModelKt;
import com.myapp.lexicon.ads.BannersActivityKt;
import com.myapp.lexicon.ads.InterstitialAdIds;
import com.myapp.lexicon.ads.NativeAdsActivityKt;
import com.myapp.lexicon.ads.RevenueViewModel;
import com.myapp.lexicon.ads.models.AdData;
import com.myapp.lexicon.ads.models.AdType;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.common.CommonConstantsKt;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
import com.parse.ParseUser;
import com.yandex.mobile.ads.interstitial.InterstitialAd;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;


public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    private ServiceDialogActivityBinding binding;
    private AuthViewModel authVM;
    private AdsViewModel adsVM;
    private RevenueViewModel revenueVM;
    private InterstitialAd interstitialAd;
    private LockOrientation locker;

    private static Long lastAdShowTime = 0L;

    @Override
    public void openApp()
    {
        finish();
        startActivity(new Intent(this, SplashActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        locker = new LockOrientation(this);
        locker.lock();

        binding = ServiceDialogActivityBinding.inflate(getLayoutInflater(), new FrameLayout(this), false);
        setContentView(binding.getRoot());

        adsVM = new ViewModelProvider(ServiceActivity.this).get(AdsViewModel.class);
        revenueVM = new ViewModelProvider(ServiceActivity.this).get(RevenueViewModel.class);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            handleAdvertisingPayload();
        }
        else {
            authVM = new ViewModelProvider(this).get(AuthViewModel.class);
            SettingsExtKt.getAuthDataFromPref(
                    this,
                    () -> null,
                    (email, password) -> {
                        authVM.signInWithEmailAndPassword(email, password);
                        authVM.getState().observe(this, userState -> {
                            userState.onSignIn(
                                    user -> {
                                        handleAdvertisingPayload();
                                        return null;
                                    }
                            );
                            userState.onFailure(
                                    e -> {
                                        String message = (e.getMessage() == null) ? ServiceActivity.class.getSimpleName().concat(" - Unknown error") : e.getMessage();
                                        ExtensionsKt.showSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG);
                                        return null;
                                    }
                            );
                        });
                        return null;
                    },
                    e -> {
                        String message = (e.getMessage() == null) ? ServiceActivity.class.getSimpleName().concat(" - Unknown error") : e.getMessage();
                        ExtensionsKt.showSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG);
                        return null;
                    }
            );
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        int displayMode = 0;
        try
        {
            displayMode = Integer.parseInt(preferencesString);
        } catch (NumberFormatException e)
        {
            ExtensionsKt.printStackTraceIfDebug(e);
        }

        if (displayMode == 0)
        {
            RepeatDialog modalFragment = RepeatDialog.Companion.newInstance(this);
            modalFragment.show(getSupportFragmentManager().beginTransaction(), RepeatDialog.Companion.getTAG());
        }
        else if (displayMode == 1)
        {
            TestModeDialog testModalFragment = TestModeDialog.Companion.newInstance(this);
            testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModeDialog.Companion.getTAG());
        }
    }

    private void handleAdvertisingPayload() {

        long diffTime = System.currentTimeMillis() - lastAdShowTime;
        if (diffTime < 60000)
        {
            return;
        }

        AdType adType = CommonConstantsKt.getAD_TYPE();
        if (adType == AdType.INTERSTITIAL) {
            adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_2);
            adsVM.getInterstitialAd().observe(ServiceActivity.this, result -> {
                interstitialAd = adsVM.getInterstitialAdOrNull();
                if (interstitialAd != null) {
                    AdsViewModelKt.showAd(
                            interstitialAd,
                            ServiceActivity.this,
                            () -> null,
                            adData -> {
                                if (adData != null)
                                {
                                    revenueVM.updateUserRevenueIntoCloud(adData);
                                }
                                else {
                                    AdData emptyAdData = new AdData("", "", "", "", null, "", "", 0.0, 0.0);
                                    revenueVM.updateUserRevenueIntoCloud(emptyAdData);
                                }
                                return null;
                            },
                            bonus -> {
                                adsVM.setInterstitialAdState(new AdsViewModel.AdState.Dismissed(bonus));
                                return null;
                            }
                    );
                }
                else {
                    adsVM.setInterstitialAdState(new AdsViewModel.AdState.Dismissed(0.0));
                }
            });
        }
        if (adType == AdType.BANNER) {
            BannersActivityKt.startBannersActivity(
                    this,
                    adData -> {
                        if (adData != null) {
                            revenueVM.updateUserRevenueIntoCloud(adData);
                        }
                        else {
                            AdData emptyAdData = new AdData("", "", "", "", null, "", "", 0.0, 0.0);
                            revenueVM.updateUserRevenueIntoCloud(emptyAdData);
                        }
                        return null;
                    },
                    bonus -> {
                        adsVM.setInterstitialAdState(new AdsViewModel.AdState.Dismissed(bonus));
                        return null;
                    }
            );
        }
        if (adType == AdType.NATIVE) {
            NativeAdsActivityKt.startNativeAdsActivity(
                    this,
                    adData -> {
                        if (adData != null) {
                            revenueVM.updateUserRevenueIntoCloud(adData);
                        }
                        else {
                            AdData emptyAdData = new AdData("", "", "", "", null, "", "", 0.0, 0.0);
                            revenueVM.updateUserRevenueIntoCloud(emptyAdData);
                        }
                        return null;
                    },
                    bonus -> {
                        adsVM.setInterstitialAdState(new AdsViewModel.AdState.Dismissed(bonus));
                        return null;
                    }
            );
        }
    }

    @Override
    protected void onDestroy()
    {
        lastAdShowTime = System.currentTimeMillis();
        locker.unLock();
        super.onDestroy();
    }
}


