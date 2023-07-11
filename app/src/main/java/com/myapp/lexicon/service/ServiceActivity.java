package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.appodeal.ads.Appodeal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.ads.AdsExtensionsKt;
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
    private UserViewModel userVM;

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
        setContentView(R.layout.service_dialog_activity);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            userVM = new ViewModelProvider(this).get(UserViewModel.class);
            userVM.getUserFromCloud(firebaseUser.getUid());

            userVM.getState().observe(this, state -> {
                if (state instanceof UserViewModel.State.ReceivedUserData) {
                    User user = ((UserViewModel.State.ReceivedUserData) state).getUser();
                    boolean isInitialized = Appodeal.isInitialized(Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO);
                    if (!isInitialized)
                    {
                        AdsExtensionsKt.adsInitialize(
                                this,
                                Appodeal.REWARDED_VIDEO,
                                () -> {
                                    AdsExtensionsKt.adRevenueInfo(this, revenueInfo -> {
                                        double revenue = revenueInfo.getRevenue();
                                        String currency = revenueInfo.getCurrency();
                                        user.setTotalRevenue(revenue);
                                        user.setCurrency(currency);
                                        userVM.updateUserRevenue(revenue, user);
                                        return null;
                                    });

                                    boolean adsIsEnabled = SettingsExtKt.getAdsIsEnabled(this);
                                    if (adsIsEnabled) {
                                        AdsExtensionsKt.showInterstitial(
                                                this,
                                                Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO,
                                                () -> null,
                                                () -> null,
                                                () -> null
                                        );
                                    }
                                    return null;
                                },
                                apdInitializationErrors -> {
                                    if (BuildConfig.DEBUG) {
                                        apdInitializationErrors.forEach(Throwable::printStackTrace);
                                    }
                                    return null;
                                }
                        );
                    }
                }
            });
        }

        MainViewModel vm = new ViewModelProvider(this).get(MainViewModel.class);

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
                vm.getCountersById(dictName, id);
            }
            vm.getWordCounters().observe(this, counters -> {
                if (counters != null && counters.size() > 1)
                {

                    if (finalDisplayMode == 0)
                    {
                        ModalFragment modalFragment = ModalFragment.newInstance(json, counters, this);
                        modalFragment.show(getSupportFragmentManager().beginTransaction(), ModalFragment.TAG);
                        vm.goForward(Arrays.asList(words));
                    }
                    else if (finalDisplayMode == 1)
                    {
                        TestModalFragment testModalFragment = TestModalFragment.newInstance(json, counters, this);
                        testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModalFragment.TAG);
                    }
                }
            });
        }
        else finish();
    }


}
