@file:Suppress("DEPRECATION")

package com.myapp.lexicon.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.DonateViewModel
import com.myapp.lexicon.dialogs.DisableAdsDialog
import com.myapp.lexicon.helpers.noAdsToken
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.schedule.AlarmScheduler
import com.myapp.lexicon.service.LexiconService
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat()
{
    private lateinit var listDisplayModePref: ListPreference
    private lateinit var serviceCheckBoxPref: CheckBoxPreference
    private lateinit var showIntervalsPref: ListPreference
    private val billingVM: DonateViewModel by activityViewModels()
    private val disableAdsDialog = DisableAdsDialog()

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
            onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener
            {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
                {
                    return try
                    {
                        value = newValue.toString()
                        summary = entry
                        (requireActivity() as MainActivity).testIntervalOnChange(value.toInt())
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

        listDisplayModePref = findPreference(requireActivity().getString(R.string.key_list_display_mode))!!
        // при новом создании экрана заполняем summary значением настройки
        listDisplayModePref.summary = listDisplayModePref.entry
        listDisplayModePref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listDisplayModePref.value = newValue.toString()
                listDisplayModePref.summary = listDisplayModePref.entry
                return true
            }
        }

        val listOnUnBlockingScreen = findPreference<ListPreference>(requireContext().getString(R.string.key_display_variant))!!
        listOnUnBlockingScreen.summary = listOnUnBlockingScreen.entry
        listOnUnBlockingScreen.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listOnUnBlockingScreen.value = newValue.toString()
                listOnUnBlockingScreen.summary = listOnUnBlockingScreen.entry
                
                if (newValue.toString().toInt() == 0)
                {
                    if (ContextCompat.checkSelfPermission(requireContext(), "") != PackageManager.PERMISSION_GRANTED)
                    {
                        view?.let { redirectIfXiaomiDevice() }
                    }
                }

                return true
            }
        }

        serviceCheckBoxPref = findPreference(requireContext().getString(R.string.key_service))!!
        serviceCheckBoxPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listDisplayModePref.isEnabled = (newValue as Boolean)
                listOnUnBlockingScreen.isEnabled = newValue
                if (!newValue)
                {
                    requireContext().stopService(Intent(requireContext(), LexiconService::class.java))
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

                if (newValue)
                {
                    if (ContextCompat.checkSelfPermission(requireContext(), "") != PackageManager.PERMISSION_GRANTED)
                    {
                        view?.let { redirectIfXiaomiDevice() }
                    }
                }

                return true
            }

        }

        showIntervalsPref = findPreference(requireContext().getString(R.string.key_show_intervals))!!
        showIntervalsPref.summary = showIntervalsPref.entry
        showIntervalsPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
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

    @Suppress("ObjectLiteralToLambda")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))

//        billing.noAdsToken.observe(viewLifecycleOwner) {
//            it?.let { token ->
//                if (token.isEmpty()) {
//                    findPreference<PreferenceCategory>("disableAdsCategory")?.isEnabled = true
//                    findPreference<SwitchPreferenceCompat>("disableAds")?.isChecked = true
//                } else {
//                    findPreference<PreferenceCategory>("disableAdsCategory")?.isEnabled = false
//                    findPreference<SwitchPreferenceCompat>("disableAds")?.isChecked = false
//                }
//            }
//        }

        if (noAdsToken.isEmpty()) {
            findPreference<PreferenceCategory>("disableAdsCategory")?.isEnabled = true
            findPreference<SwitchPreferenceCompat>("disableAds")?.isChecked = true
        }
        else {
            findPreference<PreferenceCategory>("disableAdsCategory")?.isEnabled = false
            findPreference<SwitchPreferenceCompat>("disableAds")?.isChecked = false
        }

        val noAdsSwitch = findPreference<SwitchPreferenceCompat>("disableAds")
        noAdsSwitch?.apply {
            onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener
            {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
                {
                    if (newValue == false)
                    {
                        disableAdsDialog.show(requireActivity().supportFragmentManager, "").run {
                            disableAdsDialog.isCancel.observe(viewLifecycleOwner) {
                                if (it) {
                                    noAdsSwitch.isChecked = true
                                }
                            }
                        }
                    }
                    return true
                }
            }
        }

//        billing.wasCancelled.observe(viewLifecycleOwner) {
//            noAdsSwitch?.isChecked = true
//        }
        billingVM.wasCancelled.observe(viewLifecycleOwner) {
            noAdsSwitch?.isChecked = true
        }

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