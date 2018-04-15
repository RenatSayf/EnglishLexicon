package com.myapp.lexicon.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.View;

import com.myapp.lexicon.R;

/**
 * Created by Renat
 */

public class SettingsFragment extends PreferenceFragment
{
    ListPreference listDisplayModePref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        listDisplayModePref = (ListPreference) findPreference("list_display_mode");
        // при новом создании экрана заполняем summary значением настройки
        listDisplayModePref.setSummary(listDisplayModePref.getEntry());

        listDisplayModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                listDisplayModePref.setValue(newValue.toString());
                listDisplayModePref.setSummary(listDisplayModePref.getEntry());
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));

    }
}
