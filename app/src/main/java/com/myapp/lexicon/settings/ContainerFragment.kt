package com.myapp.lexicon.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentSettingsContainerBinding

class ContainerFragment: Fragment() {

    private lateinit var binding: FragmentSettingsContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.beginTransaction()
            .add(R.id.setting_container, SettingsFragment::class.java, null, null)
            .commit()
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}