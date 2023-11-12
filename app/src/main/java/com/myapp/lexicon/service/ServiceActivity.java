package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsViewModel;
import com.myapp.lexicon.ads.AdsViewModelKt;
import com.myapp.lexicon.ads.InterstitialAdIds;
import com.myapp.lexicon.ads.RevenueViewModel;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.models.WordKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
import com.parse.ParseUser;
import com.yandex.mobile.ads.interstitial.InterstitialAd;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    private ServiceDialogActivityBinding binding;
    private AuthViewModel authVM;
    private AdsViewModel adsVM;
    private RevenueViewModel revenueVM;
    private InterstitialAd interstitialAd;
    private LockOrientation locker;

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

        binding = ServiceDialogActivityBinding.inflate(getLayoutInflater(), new LinearLayout(this), false);
        setContentView(binding.getRoot());

        adsVM = new ViewModelProvider(this).get(AdsViewModel.class);
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

        MainViewModel mainVM = new ViewModelProvider(this).get(MainViewModel.class);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        int displayMode = 0;
        try
        {
            displayMode = Integer.parseInt(preferencesString);
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        int finalDisplayMode = displayMode;

        String json = getIntent().getStringExtra(ServiceActivity.ARG_JSON);
        if (json != null)
        {
            List<Word> words = WordKt.toWordList(json);
            if (words.size() > 0)
            {
                String dictName = words.get(0).getDictName();
                int id = words.get(0).get_id();
                mainVM.getCountersById(dictName, id);
            }
            mainVM.getWordCounters().observe(this, counters -> {
                if (counters != null && counters.size() > 1)
                {

                    if (finalDisplayMode == 0)
                    {
                        RepeatDialog modalFragment = RepeatDialog.Companion.newInstance(json, counters, this);
                        modalFragment.show(getSupportFragmentManager().beginTransaction(), RepeatDialog.Companion.getTAG());
                    }
                    else if (finalDisplayMode == 1)
                    {
                        TestModeDialog testModalFragment = TestModeDialog.Companion.newInstance(json, counters, this);
                        testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModeDialog.Companion.getTAG());
                    }
                }
            });
        }
        else
        {
            String message = ServiceActivity.class.getSimpleName().concat(" - json is null");
            ExtensionsKt.showToastIfDebug(this, message);
            finish();
        }
    }

    private void handleAdvertisingPayload() {

        adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_2);
        adsVM.getInterstitialAd().observe(this, result -> {
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
                            return null;
                        },
                        () -> null
                );
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        locker.unLock();
        super.onDestroy();
    }
}


