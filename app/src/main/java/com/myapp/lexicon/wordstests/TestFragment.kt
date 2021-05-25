package com.myapp.lexicon.wordstests

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.databinding.TestFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class TestFragment : Fragment(R.layout.test_fragment)
{

    companion object
    {
        fun newInstance() = TestFragment()
    }

    private lateinit var binding: TestFragmentBinding
    private lateinit var testVM: TestViewModel
    private val composite = CompositeDisposable()

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

        composite.add(
            RxTextView.textChanges(binding.editTextView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe({ chars ->
                    if (chars.isNotEmpty())
                    {
                        val dict = binding.btnViewDict.text.toString()
                        testVM.getAllSimilarWords(dict, "%${chars}%")
                    }
                }, { t ->
                    t.printStackTrace()
                }))

        testVM.similarWords.observe(viewLifecycleOwner, { words ->
            if (words.isNotEmpty())
            {
                val translates = arrayListOf<String>()
                words.forEach {
                    translates.add(it.translate)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, translates)
                binding.editTextView.apply {
                    setAdapter(adapter)
                }

            }
        })

        binding.editTextView.setOnItemClickListener(object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                val translateText = (p1 as TextView).text.toString()
                val enText = binding.enWordTV.text.toString()
                Word(-1, "", enText, translateText, 1)
            }

        })

        binding.clearBtnView.setOnClickListener {
            binding.editTextView.text.clear()
        }
    }

    override fun onDestroy()
    {
        composite.apply {
            dispose()
            clear()
        }
        super.onDestroy()
    }

}