package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.common.AdsSource;
import com.myapp.lexicon.databinding.ServiceDialogActivityBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.splash.SplashActivity;
import com.parse.ParseUser;

import java.util.concurrent.TimeUnit;

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
    private LockOrientation locker;
    private AlarmScheduler scheduler;


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

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null)
        {
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
            getSupportFragmentManager().beginTransaction().add(R.id.layoutDialog, modalFragment).commit();
        }
        else if (displayMode == 1)
        {
            TestModeDialog testModalFragment = TestModeDialog.Companion.newInstance(this);
            getSupportFragmentManager().beginTransaction().add(R.id.layoutDialog, testModalFragment).commit();
        }
    }

    @Override
    protected void onDestroy()
    {
        //noinspection ConstantValue
        if (BuildConfig.ADS_SOURCE.equals(AdsSource.TEST_AD.name()))
        {
            long millis = TimeUnit.MINUTES.toMillis(1);
            scheduler.scheduleOne(millis);
        } else
        {
            long repeatingInterval = SettingsExtKt.getNotificationRepeatingInterval(this);
            scheduler.scheduleOne(repeatingInterval);
        }

        locker.unLock();
        super.onDestroy();
    }

}


