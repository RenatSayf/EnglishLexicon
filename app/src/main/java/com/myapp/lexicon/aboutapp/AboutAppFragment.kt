@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.aboutapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentAboutAppBinding
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.settings.goToAppStore

/** @noinspection CodeBlock2Expr
 */
class AboutAppFragment : Fragment() {

    private var binding: FragmentAboutAppBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            toolBar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            try {
                val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
                val displayedText = "v." + packageInfo.versionName
                versionNameTv.text = displayedText
            } catch (e: PackageManager.NameNotFoundException) {
                versionNameTv.text = "???"
            }

            btnEvaluate.setOnClickListener {
                requireContext().goToAppStore()
            }
            tvLinkPrivacyPolicy.setOnClickListener {
                tvLinkPrivacyPolicy.setTextColor(Color.RED)
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)))
                startActivity(intent)
            }

            requireContext().checkAppUpdate(
                onAvailable = {
                    tvUpdateInfo.apply {
                        this.text = getString(R.string.text_update_available)
                        visibility = View.VISIBLE
                    }
                    btnUpdate.visibility = View.VISIBLE
                },
                onNotAvailable = {
                    tvUpdateInfo.visibility = View.GONE
                    btnUpdate.visibility = View.GONE
                }
            )

            btnUpdate.setOnClickListener {
                requireContext().goToAppStore()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity.supportFragmentManager.popBackStack()
                this.remove()
            }
        })
    }
}
