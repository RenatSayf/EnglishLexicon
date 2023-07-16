package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsExtensionsKt;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.viewmodels.UserViewModel;
import com.myapp.lexicon.models.User;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;

import java.util.Arrays;

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

        if (firebaseUser != null) {
            handleAdvertisingPayload(firebaseUser.getUid());
        }
        else {
            authVM = new ViewModelProvider(this).get(AuthViewModel.class);
            SettingsExtKt.getAuthDataFromPref(
                    this,
                    () -> null,
                    (id, email, password) -> {
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
            Word[] words = StringOperations.getInstance().jsonToWord(json);
            if (words.length > 0)
            {
                String dictName = words[0].getDictName();
                int id = words[0].get_id();
                mainVM.getCountersById(dictName, id);
            }
            mainVM.getWordCounters().observe(this, counters -> {
                if (counters != null && counters.size() > 1)
                {

                    if (finalDisplayMode == 0)
                    {
                        RepeatDialog modalFragment = RepeatDialog.Companion.newInstance(json, counters, this);
                        modalFragment.show(getSupportFragmentManager().beginTransaction(), RepeatDialog.Companion.getTAG());
                        mainVM.goForward(Arrays.asList(words));
                    }
                    else if (finalDisplayMode == 1)
                    {
                        TestModalFragment testModalFragment = TestModalFragment.newInstance(json, counters, this);
                        testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModalFragment.TAG);
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

        userVM.getState().observe(this, state -> {

            boolean isInitialized = Appodeal.isInitialized(Appodeal.REWARDED_VIDEO | Appodeal.INTERSTITIAL);
            if (!isInitialized) {
                AdsExtensionsKt.adsInitialize(
                        this,
                        Appodeal.REWARDED_VIDEO | Appodeal.INTERSTITIAL,
                        () -> null,
                        errors -> {
                            errors.forEach(error -> {
                                String message = error.getMessage() == null ? ServiceActivity.class.getSimpleName().concat(": ad initialize error") : error.getMessage();
                                ExtensionsKt.showToastIfDebug(ServiceActivity.this, message);
                            });
                            return null;
                        }
                );
            }

            if (state instanceof UserViewModel.State.ReceivedUserData) {
                User user = ((UserViewModel.State.ReceivedUserData) state).getUser();
                AdsExtensionsKt.adRevenueInfo(this, revenueInfo -> {
                    double revenue = revenueInfo.getRevenue();
                    String currency = revenueInfo.getCurrency();
                    user.setTotalRevenue(revenue);
                    user.setCurrency(currency);
                    userVM.updateUserRevenue(revenue, user);
                    return null;
                });
                AdsExtensionsKt.showInterstitial(
                        this,
                        Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO,
                        () -> null,
                        () -> null,
                        err -> {
                            if (BuildConfig.DEBUG) {
                                ExtensionsKt.showToast(this, err, Toast.LENGTH_LONG);
                            }
                            return null;
                        }
                );
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }
}
