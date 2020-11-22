@file:Suppress("DEPRECATION")

package com.myapp.lexicon.settings

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceFragment
import android.view.View
import com.myapp.lexicon.R
import com.myapp.lexicon.main.MainActivity

/**
 * Created by Renat
 */
@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragment()
{
    private lateinit var listDisplayModePref: ListPreference
    private lateinit var serviceCheckBoxPref: CheckBoxPreference
    private lateinit var showIntervalsPref: ListPreference
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref)

        listDisplayModePref = findPreference(activity.getString(R.string.key_list_display_mode)) as ListPreference
        // при новом создании экрана заполняем summary значением настройки
        listDisplayModePref.summary = listDisplayModePref.entry
        listDisplayModePref.onPreferenceChangeListener = OnPreferenceChangeListener(fun(_: Preference, newValue: Any): Boolean
        {
            listDisplayModePref.value = newValue.toString()
            listDisplayModePref.summary = listDisplayModePref.entry
            return true
        })

        val listOnUnBlokingScreen = findPreference(activity.getString(R.string.key_on_unbloking_screen)) as ListPreference
        listOnUnBlokingScreen.summary = listOnUnBlokingScreen.entry
        listOnUnBlokingScreen.onPreferenceChangeListener = OnPreferenceChangeListener(fun(_: Preference, newValue: Any): Boolean
        {
            listOnUnBlokingScreen.value = newValue.toString()
            listOnUnBlokingScreen.summary = listOnUnBlokingScreen.entry
            AppData.getInstance().displayVariant = newValue.toString().toInt()
            return true
        })

        serviceCheckBoxPref = findPreference(activity.getString(R.string.key_service)) as CheckBoxPreference
        serviceCheckBoxPref.onPreferenceChangeListener = OnPreferenceChangeListener(fun(_: Preference?, newValue: Any?): Boolean
        {
            listDisplayModePref.isEnabled = (newValue as Boolean)
            listOnUnBlokingScreen.isEnabled = newValue
            if (MainActivity.serviceIntent != null && !newValue)
            {
                activity.stopService(MainActivity.serviceIntent)
            }
            if (newValue)
            {
                showIntervalsPref.apply {
                    value = activity.resources.getStringArray(R.array.show_intervals_values)[0]
                    summary = activity.resources.getStringArray(R.array.show_intervals)[0]
                    shouldCommit()
                }
            }
            else
            {
                if (showIntervalsPref.value == activity.resources.getStringArray(R.array.show_intervals)[0])
                {
                    listDisplayModePref.isEnabled = false
                    listOnUnBlokingScreen.isEnabled = false
                }
            }
            return true
        })

        showIntervalsPref = findPreference(activity.getString(R.string.key_show_intervals)) as ListPreference
        showIntervalsPref.summary = showIntervalsPref.entry
        showIntervalsPref.onPreferenceChangeListener = OnPreferenceChangeListener(fun(_: Preference, newValue: Any): Boolean
        {
            showIntervalsPref.value = newValue as String
            showIntervalsPref.summary = showIntervalsPref.entry
            if (newValue.toInt() != 0)
            {
                serviceCheckBoxPref.isChecked = false
                serviceCheckBoxPref.shouldCommit()
                listOnUnBlokingScreen.isEnabled = true
                listDisplayModePref.isEnabled = true
            }
            else
            {
                if (!serviceCheckBoxPref.isChecked)
                {
                    listDisplayModePref.isEnabled = false
                    listOnUnBlokingScreen.isEnabled = false
                }
            }
            return true
        })

        if (!serviceCheckBoxPref.isChecked && showIntervalsPref.value == activity.resources.getStringArray(R.array.show_intervals)[0])
        {
            listOnUnBlokingScreen.isEnabled = false
            listDisplayModePref.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))
    }
}