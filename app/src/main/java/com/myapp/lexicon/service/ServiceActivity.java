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
import com.myapp.lexicon.ads.InterstitialAdIdsKt;
import com.myapp.lexicon.ads.NativeAdsActivityKt;
import com.myapp.lexicon.ads.RewardedAdIdsKt;
import com.myapp.lexicon.ads.models.AdType;
import com.myapp.lexicon.ads.models.AdTypeKt;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.auth.account.UserDataViewModel;
import com.myapp.lexicon.common.CommonConstantsKt;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.models.RevenueX;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.EncryptedPrefKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
import com.parse.ParseUser;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.rewarded.RewardedAd;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;


/** @noinspection DataFlowIssue*/
public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    private ServiceDialogActivityBinding binding;
    private AuthViewModel authVM;
    private AdsViewModel adsVM;
    private UserDataViewModel userDataVM;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private LockOrientation locker;
    private AlarmScheduler scheduler;

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

        scheduler = new AlarmScheduler(this);

        binding = ServiceDialogActivityBinding.inflate(getLayoutInflater(), new FrameLayout(this), false);
        setContentView(binding.getRoot());

        adsVM = new ViewModelProvider(ServiceActivity.this).get(AdsViewModel.class);

        UserDataViewModel.Factory factory = new UserDataViewModel.Factory();
        userDataVM = new ViewModelProvider(this, factory).get(UserDataViewModel.class);

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
                                        SettingsExtKt.saveUserPercentToPref(this, user);
                                        handleAdvertisingPayload();
                                        return null;
                                    }
                            );
                            userState.onFailure(
                                    e -> {
                                        String message = (e.getMessage() == null) ? ServiceActivity.class.getSimpleName().concat(" - Unknown error") : e.getMessage();
                                        ExtensionsKt.showMultiLineSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG);
                                        return null;
                                    }
                            );
                        });
                        return null;
                    },
                    e -> {
                        String message = (e.getMessage() == null) ? ServiceActivity.class.getSimpleName().concat(" - Unknown error") : e.getMessage();
                        ExtensionsKt.showMultiLineSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG);
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
        if (diffTime < CommonConstantsKt.getAD_SHOWING_INTERVAL_IN_SEC() * 1000)
        {
            adsVM.setAdState(new AdsViewModel.AdState.Dismissed(0.0));
            return;
        }

        String accessToken = EncryptedPrefKt.getAccessToken(ServiceActivity.this);
        int adType = AdTypeKt.getAD_SERVICE(ServiceActivity.this);
        if (adType == AdType.INTERSTITIAL.getType()) {
            adsVM.getInterstitialAd().observe(ServiceActivity.this, result -> {
                interstitialAd = adsVM.getInterstitialAdOrNull();
                if (interstitialAd != null) {
                    AdsViewModelKt.showAd(
                            interstitialAd,
                            ServiceActivity.this,
                            () -> null,
                            adData -> {
                                if (adData != null && !accessToken.isEmpty())
                                {
                                    RevenueX revenue = com.myapp.lexicon.ads.ext.ExtensionsKt.toRevenue(adData);
                                    userDataVM.updateUserBalance(accessToken, revenue);
                                }
                                return null;
                            },
                            bonus -> {
                                adsVM.setAdState(new AdsViewModel.AdState.Dismissed(bonus));
                                return null;
                            }
                    );
                }
                else {
                    adsVM.setAdState(new AdsViewModel.AdState.Dismissed(0.0));
                }
            });
            adsVM.loadInterstitialAd(InterstitialAdIdsKt.getINTERSTITIAL_SERVICE());
        }
        if (adType == AdType.BANNER.getType()) {
            BannersActivityKt.startBannersActivity(
                    this,
                    adData -> {
                        if (adData != null && !accessToken.isEmpty()) {
                            RevenueX revenue = com.myapp.lexicon.ads.ext.ExtensionsKt.toRevenue(adData);
                            userDataVM.updateUserBalance(accessToken, revenue);
                        }
                        return null;
                    },
                    bonus -> {
                        adsVM.setAdState(new AdsViewModel.AdState.Dismissed(bonus));
                        return null;
                    }
            );
        }
        if (adType == AdType.NATIVE.getType()) {
            NativeAdsActivityKt.startNativeAdsActivity(
                    this,
                    adsReward -> {
                        adsVM.setAdState(new AdsViewModel.AdState.Rewarded(adsReward));
                        return null;
                    },
                    error -> {
                        ExtensionsKt.printLogIfDebug(error);
                        return null;
                    }
            );
        }
        if (adType == AdType.REWARDED.getType()) {
            adsVM.getRewardedAd().observe(ServiceActivity.this, result -> {
                rewardedAd = adsVM.getRewardedAdOrNull();
                if (rewardedAd != null) {
                    AdsViewModelKt.showAd(
                            rewardedAd,
                            ServiceActivity.this,
                            () -> null,
                            adData -> {
                                if (adData != null && !accessToken.isEmpty())
                                {
                                    RevenueX revenue = com.myapp.lexicon.ads.ext.ExtensionsKt.toRevenue(adData);
                                    userDataVM.updateUserBalance(accessToken, revenue);
                                }
                                return null;
                            },
                            bonus -> {
                                adsVM.setAdState(new AdsViewModel.AdState.Dismissed(bonus));
                                return null;
                            }
                    );
                }
            });
            adsVM.loadRewardedAd(RewardedAdIdsKt.getREWARDED_SERVICE_ID());
        }
    }

    @Override
    protected void onDestroy()
    {
        long repeatingInterval = SettingsExtKt.getNotificationRepeatingInterval(this);
        scheduler.scheduleOne(repeatingInterval);
        lastAdShowTime = System.currentTimeMillis();
        interstitialAd = null;
        rewardedAd = null;
        locker.unLock();
        super.onDestroy();
    }

}


