@file:Suppress("DEPRECATION")

package com.myapp.lexicon.settings

import android.os.Bundle
import android.view.View
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.schedule.AlarmScheduler

/**
 * Created by Renat
 */

class SettingsFragment : PreferenceFragmentCompat()
{
    private lateinit var listDisplayModePref: ListPreference
    private lateinit var serviceCheckBoxPref: CheckBoxPreference
    private lateinit var showIntervalsPref: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        setPreferencesFromResource(R.xml.pref, rootKey)
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val wordsInterval = findPreference<ListPreference>(requireContext().getString(R.string.key_learning_mode))?.apply {
            summary = this.entry
            onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener
            {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
                {
                    value = newValue.toString()
                    summary = entry
                    return true
                }

            }
        }

        listDisplayModePref = findPreference(requireActivity().getString(R.string.key_list_display_mode))!!
        // при новом создании экрана заполняем summary значением настройки
        listDisplayModePref.summary = listDisplayModePref.entry
        listDisplayModePref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
            {
                listDisplayModePref.value = newValue.toString()
                listDisplayModePref.summary = listDisplayModePref.entry
                return true
            }
        }

        val listOnUnBlockingScreen = findPreference<ListPreference>(requireContext().getString(R.string.key_display_variant))!!
        listOnUnBlockingScreen.summary = listOnUnBlockingScreen.entry
        listOnUnBlockingScreen.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
            {
                listOnUnBlockingScreen.value = newValue.toString()
                listOnUnBlockingScreen.summary = listOnUnBlockingScreen.entry
                AppData.getInstance().displayVariant = newValue.toString().toInt()
                return true
            }
        }

        serviceCheckBoxPref = findPreference(requireContext().getString(R.string.key_service))!!
        serviceCheckBoxPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
            {
                listDisplayModePref.isEnabled = (newValue as Boolean)
                listOnUnBlockingScreen.isEnabled = newValue
                if (MainActivity.serviceIntent != null && !newValue)
                {
                    requireContext().stopService(MainActivity.serviceIntent)
                }
                if (newValue)
                {
                    showIntervalsPref.apply {
                        value = requireContext().resources.getStringArray(R.array.show_intervals_values)[0]
                        summary = requireContext().resources.getStringArray(R.array.show_intervals)[0]
                    }
                }
                else
                {
                    if (showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
                    {
                        listDisplayModePref.isEnabled = false
                        listOnUnBlockingScreen.isEnabled = false
                    }
                }
                return true
            }

        }

        showIntervalsPref = findPreference(requireContext().getString(R.string.key_show_intervals))!!
        showIntervalsPref.summary = showIntervalsPref.entry
        showIntervalsPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
            {
                showIntervalsPref.value = newValue as String
                showIntervalsPref.summary = showIntervalsPref.entry
                if (newValue.toInt() != 0)
                {
                    serviceCheckBoxPref.isChecked = false
                    listOnUnBlockingScreen.isEnabled = true
                    listDisplayModePref.isEnabled = true
                }
                else
                {
                    AlarmScheduler(requireActivity()).cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION)
                    if (!serviceCheckBoxPref.isChecked)
                    {
                        listDisplayModePref.isEnabled = false
                        listOnUnBlockingScreen.isEnabled = false
                    }
                }
                return true
            }
        }

        if (!serviceCheckBoxPref.isChecked && showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
        {
            listOnUnBlockingScreen.isEnabled = false
            listDisplayModePref.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))
    }


}