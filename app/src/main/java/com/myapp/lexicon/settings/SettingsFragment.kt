@file:Suppress("DEPRECATION", "ObjectLiteralToLambda")

package com.myapp.lexicon.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.schedule.AlarmScheduler
import java.util.Locale


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

        findPreference<ListPreference>(requireContext().getString(R.string.key_test_interval))?.apply {
            summary = this.entry
            onPreferenceChangeListener = object : OnPreferenceChangeListener
            {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
                {
                    return try
                    {
                        value = newValue.toString()
                        summary = entry
                        setFragmentResult(getString(R.string.KEY_TEST_INTERVAL_CHANGED), Bundle.EMPTY)
                        true
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                        false
                    }
                }
            }
        }

        listDisplayModePref = findPreference(requireContext().getString(R.string.key_list_display_mode))!!
        // при новом создании экрана заполняем summary значением настройки
        listDisplayModePref.summary = listDisplayModePref.entry
        listDisplayModePref.onPreferenceChangeListener = object : OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listDisplayModePref.value = newValue.toString()
                listDisplayModePref.summary = listDisplayModePref.entry
                return true
            }
        }

        serviceCheckBoxPref = findPreference(requireContext().getString(R.string.key_service))!!
        serviceCheckBoxPref.onPreferenceChangeListener = object : OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listDisplayModePref.isEnabled = (newValue as? Boolean)?: false

                if (newValue == true)
                {
                    showIntervalsPref.apply {
                        value = requireContext().resources.getStringArray(R.array.show_intervals_values)[0]
                        summary = requireContext().resources.getStringArray(R.array.show_intervals)[0]
                    }
                }
                else if (newValue == null)
                {
                    if (showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
                    {
                        listDisplayModePref.isEnabled = false
                    }
                }
                return true
            }
        }

        showIntervalsPref = findPreference(requireContext().getString(R.string.key_show_intervals))!!
        showIntervalsPref.summary = showIntervalsPref.entry
        showIntervalsPref.onPreferenceChangeListener = object : OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                showIntervalsPref.value = newValue as String
                showIntervalsPref.summary = showIntervalsPref.entry
                val interval = try {
                    newValue.toInt()
                }
                catch (e: NumberFormatException) {
                    0
                }
                if (interval != 0)
                {
                    serviceCheckBoxPref.isChecked = false
                    listDisplayModePref.isEnabled = true
                    //view?.let { redirectIfXiaomiDevice() }
                }
                else
                {
                    AlarmScheduler(requireActivity()).cancel(AlarmScheduler.ONE_SHOOT_ACTION)
                    if (!serviceCheckBoxPref.isChecked)
                    {
                        listDisplayModePref.isEnabled = false
                    }
                }
                return true
            }
        }

        if (!serviceCheckBoxPref.isChecked && showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
        {
            listDisplayModePref.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))

        val swBatterySaving = findPreference<SwitchPreferenceCompat>("swBatterySaving")
        if (swBatterySaving != null) {
            configurePowerSettings(swBatterySaving)

            swBatterySaving.onPreferenceChangeListener = object : OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        putExtra(KEY_BATTERY_SETTINGS, BATTERY_SETTINGS)
                    }
                    startActivityForResult(intent, BATTERY_SETTINGS)
                    return true
                }
            }
        }

    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val switch = findPreference<SwitchPreferenceCompat>("swBatterySaving")
        if (requestCode == BATTERY_SETTINGS && switch != null) {
            configurePowerSettings(switch)
        }
    }

    private fun configurePowerSettings(switch: SwitchPreferenceCompat) {
        requireContext().isIgnoringBatteryOptimizations(
            onOptimizingUse = {
                switch.apply {
                    isChecked = true
                    title = getString(R.string.text_battery_saving_enabled)
                    summary = getString(R.string.text_battery_optimization_explain)
                }
            },
            onNotUse = {
                switch.apply {
                    isChecked = false
                    title = getString(R.string.text_battery_saving_disabled)
                    summary = ""
                }
            }
        )
    }

    private fun redirectIfXiaomiDevice()
    {
        if (Build.MANUFACTURER.toLowerCase(Locale.ROOT) == "xiaomi")
        {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName("com.miui.securitycenter","com.miui.permcenter.permissions.PermissionsEditorActivity")
            intent.putExtra("extra_pkgname", (requireActivity() as MainActivity).packageName)
            startActivity(intent)
            Toast.makeText(requireContext(), getString(R.string.text_enabled_permission_pop_up), Toast.LENGTH_LONG).show()
        }
    }



}