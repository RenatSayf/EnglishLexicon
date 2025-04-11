package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsViewModel;
import com.myapp.lexicon.ads.feed_ad.FeedAdsActivityKt;
import com.myapp.lexicon.ads.interstitial.InterstitialAdExtKt;
import com.myapp.lexicon.ads.models.AdType;
import com.myapp.lexicon.ads.native_ad.NativeAdsActivityKt;
import com.myapp.lexicon.ads.rewarded.RewardedAdExtKt;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.common.CommonConstantsKt;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.di.App;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.models.AppConfig;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
import com.parse.ParseUser;

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

        AppConfig config = App.Companion.getINSTANCE().getDefaultConfig();

        if (config.getAdTypePerScreen().getService() == AdType.INTERSTITIAL.getType()) {
            InterstitialAdExtKt.loadInterstitialAd(
                    this,
                    config.getInterstitialAdIds().getService(),
                    interstitialAd -> {
                        InterstitialAdExtKt.showInterstitialAd(
                                this,
                                reward -> {
                                    adsVM.setAdState(new AdsViewModel.AdState.Rewarded(reward));
                                    return null;
                                },
                                () -> null
                        );
                        return null;
                    }
            );
        }
        if (config.getAdTypePerScreen().getService() == AdType.NATIVE.getType()) {
            NativeAdsActivityKt.startNativeAdsActivity(
                    this,
                    config.getNativeIds().getService(),
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
        if (config.getAdTypePerScreen().getService() == AdType.REWARDED.getType()) {
            RewardedAdExtKt.loadRewardedAd(
                    this,
                    config.getRewardedIds().getService(),
                    rewardedAd1 -> {
                        RewardedAdExtKt.showRewardedAd(
                                this,
                                reward -> {
                                    adsVM.setAdState(new AdsViewModel.AdState.Rewarded(reward));
                                    return null;
                                },
                                () -> null
                        );
                        return null;
                    }
            );
        }
        if (config.getAdTypePerScreen().getService() == AdType.FEED.getType())
        {
            FeedAdsActivityKt.startFeedAdsActivity(
                    this,
                    config.getFeedIds().getService(),
                    reward -> {
                        adsVM.setAdState(new AdsViewModel.AdState.Rewarded(reward));
                        return null;
                    },
                    error -> {
                        ExtensionsKt.printLogIfDebug(error);
                        return null;
                    }
            );
        }
    }

    @Override
    protected void onDestroy()
    {
        long repeatingInterval = SettingsExtKt.getNotificationRepeatingInterval(this);
        scheduler.scheduleOne(repeatingInterval);
        lastAdShowTime = System.currentTimeMillis();
        locker.unLock();
        super.onDestroy();
    }

}


