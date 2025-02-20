package com.myapp.lexicon.auth.invoice

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.myapp.lexicon.common.SELF_EMPLOYED_MARKET
import com.myapp.lexicon.common.SELF_EMPLOYED_RU_STORE
import com.myapp.lexicon.databinding.FragmentInstallTaxAppBinding

class InstallTaxAppFragment : Fragment() {

    companion object {
        fun newInstance() = InstallTaxAppFragment()
    }

    private var binding: FragmentInstallTaxAppBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInstallTaxAppBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            toolBar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            btnInstall.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, SELF_EMPLOYED_MARKET))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, SELF_EMPLOYED_RU_STORE))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }
}