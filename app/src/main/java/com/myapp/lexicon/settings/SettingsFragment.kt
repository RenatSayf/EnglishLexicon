@file:Suppress("DEPRECATION")

package com.myapp.lexicon.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.*
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.BillingViewModel
import com.myapp.lexicon.cloudstorage.StorageDialog
import com.myapp.lexicon.databinding.DialogStorageBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.schedule.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat()
{
    private lateinit var listDisplayModePref: ListPreference
    private lateinit var serviceCheckBoxPref: CheckBoxPreference
    private lateinit var showIntervalsPref: ListPreference

    private val billingVM: BillingViewModel by lazy {
        ViewModelProvider(this)[BillingViewModel::class.java]
    }

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

        listDisplayModePref = findPreference(requireContext().getString(R.string.key_list_display_mode))!!
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

        serviceCheckBoxPref = findPreference(requireContext().getString(R.string.key_service))!!
        serviceCheckBoxPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
            {
                listDisplayModePref.isEnabled = (newValue as? Boolean)?: false
                //listOnUnBlockingScreen.isEnabled = (newValue as? Boolean)?: false

                if (newValue == true)
                {
                    showIntervalsPref.apply {
                        value = requireContext().resources.getStringArray(R.array.show_intervals_values)[0]
                        summary = requireContext().resources.getStringArray(R.array.show_intervals)[0]
                    }
                    if (ContextCompat.checkSelfPermission(requireContext(), "") != PackageManager.PERMISSION_GRANTED)
                    {
                        view?.let { redirectIfXiaomiDevice() }
                    }
                }
                else if (newValue == null)
                {
                    if (showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
                    {
                        listDisplayModePref.isEnabled = false
                        //listOnUnBlockingScreen.isEnabled = false
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
                val interval = try {
                    newValue.toInt()
                }
                catch (e: NumberFormatException) {
                    0
                }
                if (interval != 0)
                {
                    serviceCheckBoxPref.isChecked = false
                    //listOnUnBlockingScreen.isEnabled = true
                    listDisplayModePref.isEnabled = true
                    view?.let { redirectIfXiaomiDevice() }
                }
                else
                {
                    AlarmScheduler(requireActivity()).cancel(AlarmScheduler.ONE_SHOOT_ACTION)
                    if (!serviceCheckBoxPref.isChecked)
                    {
                        listDisplayModePref.isEnabled = false
                        //listOnUnBlockingScreen.isEnabled = false
                    }
                }
                return true
            }
        }

        if (!serviceCheckBoxPref.isChecked && showIntervalsPref.value == requireContext().resources.getStringArray(R.array.show_intervals)[0])
        {
            //listOnUnBlockingScreen.isEnabled = false
            listDisplayModePref.isEnabled = false
        }
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))

        val cloudStorageSwitch = findPreference<SwitchPreferenceCompat>(getString(R.string.KEY_CLOUD_STORAGE))

        requireContext().checkCloudToken(
            onInit = {
                cloudStorageSwitch?.apply {
                    isEnabled = true
                    isChecked = false
                }
            },
            onExists = {
                cloudStorageSwitch?.apply {
                    isEnabled = false
                    isChecked = true
                }
            },
            onEmpty = {
                cloudStorageSwitch?.apply {
                    isEnabled = true
                    isChecked = false
                }
            }
        )

        findPreference<PreferenceCategory>("cloudStorageCategory")?.isEnabled = !requireContext().cloudStorageEnabled
        cloudStorageSwitch?.isChecked = requireContext().cloudStorageEnabled
        if (cloudStorageSwitch?.isChecked == true) {
            cloudStorageSwitch.isEnabled = false
            val title = cloudStorageSwitch.title
            val newTitle = "$title (${getString(R.string.text_enabled)})"
            cloudStorageSwitch.title = newTitle
        }
        cloudStorageSwitch?.let { switch ->
            switch.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {

                    if (newValue == true) {

                        val result = billingVM.cloudStorageProduct.value
                        result?.onSuccess { details ->

                            StorageDialog.newInstance(listener = object : StorageDialog.Listener {
                                private val locker = LockOrientation(requireActivity())
                                override fun onLaunch(binding: DialogStorageBinding) {
                                    locker.lock()
                                    with(binding) {
                                        tvProductName.text = details.name
                                        tvPriceTitle.text = getString(R.string.text_price)
                                        tvPriceValue.text =
                                            details.oneTimePurchaseOfferDetails?.formattedPrice
                                    }
                                }

                                override fun onDestroy() {
                                    locker.unLock()
                                }

                                override fun onPositiveClick() {
                                    billingVM.purchaseProduct(requireActivity(), details)
                                }

                                override fun onCancelClick() {
                                    switch.isChecked = false
                                }
                            }).show(parentFragmentManager, StorageDialog.TAG)
                        }
                    }
                    return true
                }
            }
        }

        billingVM.cloudStorageToken.observe(viewLifecycleOwner) { result ->
            result.onSuccess { token ->
                when {
                    token.isNotEmpty() -> {
                        cloudStorageSwitch?.let { switch ->
                            switch.isChecked = true
                            switch.isEnabled = false
                            val title = switch.title
                            val newTitle = "$title (${getString(R.string.text_enabled)})"
                            switch.title = newTitle
                        }
                        requireContext().saveCloudToken(token)
                    }
                    else -> {
                        cloudStorageSwitch?.let { switch ->
                            switch.isChecked = false
                            switch.isEnabled = true
                            switch.title = getString(R.string.text_save_my_dicts)
                        }
                    }
                }
            }
        }

        billingVM.wasCancelled.observe(viewLifecycleOwner) { result ->
            result.onSuccess { details ->
                when(details.productId) {
                    getString(R.string.id_cloud_storage) -> {
                        cloudStorageSwitch?.isChecked = false
                        cloudStorageSwitch?.isEnabled = true
                    }
                }
            }
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