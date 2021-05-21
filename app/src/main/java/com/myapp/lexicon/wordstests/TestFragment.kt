package com.myapp.lexicon.wordstests

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.TestFragmentBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestFragment : Fragment(R.layout.test_fragment)
{

    companion object
    {
        fun newInstance() = TestFragment()
    }

    private lateinit var binding: TestFragmentBinding
    private lateinit var testVM: TestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = TestFragmentBinding.bind(view)
        testVM = ViewModelProvider(this)[TestViewModel::class.java]

        testVM.currentWord.observe(viewLifecycleOwner, {
            binding.btnViewDict.text = it.dictName
        })

        testVM.wordsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty())
            {
                binding.enWordTV.text = it[0].english
                binding.ruWordTV.text = it[0].translate
            }
        })
    }

}