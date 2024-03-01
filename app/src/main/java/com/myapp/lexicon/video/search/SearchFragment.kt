@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.video.search

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentSearchBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.video.models.query.ErrorItem
import com.myapp.lexicon.video.models.query.HistoryQuery
import com.myapp.lexicon.video.models.query.SearchQuery

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private var binding: FragmentSearchBinding? = null

    private val searchVM: SearchViewModel by lazy {
        val factory = SearchViewModel.Factory()
        ViewModelProvider(this, factory)[SearchViewModel::class.java]
    }

    private val searchAdapter: SearchQueryAdapter by lazy {
        SearchQueryAdapter.getInstance()
    }

    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            rvSuggestions.apply {
                adapter = searchAdapter
            }

            if (savedInstanceState == null) {
                searchVM.getVideoHistory(
                    onStart = { locker.lock() },
                    onComplete = {t: Throwable? ->
                        locker.unLock()
                        t?.printStackTraceIfDebug()
                    },
                    onSuccess = {list: List<HistoryQuery> ->
                        if (list.isNotEmpty()) {
                            searchAdapter.submitList(list)
                        } else {
                            val errorItem = ErrorItem(getString(R.string.text_video_histori_is_empty))
                            searchAdapter.submitList(listOf(errorItem))
                        }
                    }
                )
            }

            etQuery.doOnTextChanged { text, start, before, count ->

                if (text.toString().isNotEmpty()) {
                    btnClear.visibility = View.VISIBLE
                    btnMicrophone.visibility = View.GONE

                    searchVM.fetchSuggestions(
                        query = text.toString(),
                        onStart = {
                            locker.lock()
                        },
                        onResult = { result: Result<List<SearchQuery>> ->
                            result.onSuccess { value: List<SearchQuery> ->
                                searchAdapter.submitList(value)
                            }
                            result.onFailure { exception ->
                                exception.printStackTraceIfDebug()
                            }
                        },
                        onComplete = {
                            locker.unLock()
                        }
                    )
                }
                else {
                    btnClear.visibility = View.GONE
                    btnMicrophone.visibility = View.VISIBLE

                    searchVM.getVideoHistory(
                        onStart = { locker.lock() },
                        onComplete = { t: Throwable? ->
                            locker.unLock()
                            t?.printStackTraceIfDebug()
                        },
                        onSuccess = {list: List<HistoryQuery> ->
                            if (list.isNotEmpty()) {
                                searchAdapter.submitList(list)
                            } else {
                                val errorItem = ErrorItem(getString(R.string.text_video_histori_is_empty))
                                searchAdapter.submitList(listOf(errorItem))
                            }
                        }
                    )
                }
            }

            btnClear.setOnClickListener {
                etQuery.setText("")
            }

            searchAdapter.setOnQueryItemClick { item: SearchQuery ->

            }

            searchAdapter.setOnHistoryItemClick { item: HistoryQuery ->

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

        with(binding!!) {
            btnBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

}