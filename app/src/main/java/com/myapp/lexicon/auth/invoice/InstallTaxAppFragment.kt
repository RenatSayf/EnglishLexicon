package com.myapp.lexicon.auth.invoice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myapp.lexicon.R
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

        with(binding) {

        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }
}