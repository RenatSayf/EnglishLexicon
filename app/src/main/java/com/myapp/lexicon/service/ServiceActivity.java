package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsViewModel;
import com.myapp.lexicon.ads.AdsViewModelKt;
import com.myapp.lexicon.ads.InterstitialAdIds;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.viewmodels.UserViewModel;
import com.myapp.lexicon.models.User;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.models.WordKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
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
    private UserViewModel userVM;
    private AuthViewModel authVM;
    private AdsViewModel adsVM;
    private InterstitialAd interstitialAd;

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
        binding = ServiceDialogActivityBinding.inflate(getLayoutInflater(), new LinearLayout(this), false);
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        adsVM = new ViewModelProvider(this).get(AdsViewModel.class);

        if (firebaseUser != null) {
            handleAdvertisingPayload(firebaseUser.getUid());
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
                                        handleAdvertisingPayload(user.getId());
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

    private void handleAdvertisingPayload(String userId) {
        userVM = new ViewModelProvider(ServiceActivity.this).get(UserViewModel.class);
        userVM.getUserFromCloud(userId);

        adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_4);
        adsVM.getInterstitialAd().observe(this, result -> {
            interstitialAd = adsVM.getInterstitialAdOrNull();
            if (interstitialAd != null) {
                AdsViewModelKt.showAd(
                        interstitialAd,
                        ServiceActivity.this,
                        () -> null,
                        adData -> {
                            userVM.getState().observe(this, state -> {
                                if (state instanceof UserViewModel.State.ReceivedUserData) {
                                    User user = ((UserViewModel.State.ReceivedUserData) state).getUser();
                                    if (adData != null) {
                                        userVM.updateUserRevenue(adData, user);
                                    }
                                }
                            });
                            return null;
                        },
                        () -> null,
                        () -> null
                );
            }
        });
    }

}
