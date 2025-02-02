package com.myapp.lexicon.auth.invoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.gnivts.selfemployed")
                )
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }
}