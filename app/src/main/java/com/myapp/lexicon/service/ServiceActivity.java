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
import com.myapp.lexicon.auth.account.UserDataViewModel;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.models.AppConfig;
import com.myapp.lexicon.models.Tokens;
import com.myapp.lexicon.models.UserX;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.DefaultConfigKt;
import com.myapp.lexicon.settings.EncryptedPrefKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;


/** @noinspection DataFlowIssue*/
public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    private ServiceDialogActivityBinding binding;
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
        RemoteConfigWorkerKt.scheduleRemoteConfigRequest(this);

        binding = ServiceDialogActivityBinding.inflate(getLayoutInflater(), new FrameLayout(this), false);
        setContentView(binding.getRoot());

        adsVM = new ViewModelProvider(ServiceActivity.this).get(AdsViewModel.class);

        UserDataViewModel userDataVM = new ViewModelProvider(ServiceActivity.this).get(UserDataViewModel.class);
        userDataVM.getUserState().observe(this, state -> {
            if (state instanceof UserDataViewModel.UserDataState.ReceivedUserData) {
                UserX user = ((UserDataViewModel.UserDataState.ReceivedUserData) state).getUser();
                SettingsExtKt.saveUserPercentToPref(this, user);
                handleAdvertisingPayload();
            }
            else if (state instanceof  UserDataViewModel.UserDataState.TokensUpdated) {
                Tokens tokens = ((UserDataViewModel.UserDataState.TokensUpdated) state).getTokens();
                EncryptedPrefKt.saveAuthTokens(this, tokens);
            }
            else if (state instanceof UserDataViewModel.UserDataState.AuthorizationRequired) {
                openApp();
            }
            else if (state instanceof UserDataViewModel.UserDataState.Error)
            {
                String errorMessage = ((UserDataViewModel.UserDataState.Error) state).getMessage();
                ExtensionsKt.showMultiLineSnackBar(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG);
            }
        });
        String accessToken = EncryptedPrefKt.getAccessToken(this);
        if (!accessToken.isEmpty())
        {
            userDataVM.fetchUserData(accessToken);
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

        AppConfig config = DefaultConfigKt.getCurrentConfig(this);
        long diffTime = System.currentTimeMillis() - lastAdShowTime;
        if (diffTime < config.getAdShowingIntervalInSec() * 1000L)
        {
            adsVM.setAdState(new AdsViewModel.AdState.Dismissed(0.0));
            return;
        }

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


